package com.almostreliable.merequester.terminal;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.helpers.InventoryAction;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.PatternAccessTermMenu;
import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.network.RequesterTerminalPacket;
import com.almostreliable.merequester.network.ServerToClientPacket;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.Requests;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * yoinked from {@link PatternAccessTermMenu}
 */
public class RequesterTerminalMenu extends AEBaseMenu {

    public static final MenuType<RequesterTerminalMenu> TYPE = MenuTypeBuilder
        .create(RequesterTerminalMenu::new, RequesterTerminalPart.class)
        .requirePermission(SecurityPermissions.BUILD)
        .build(MERequester.TERMINAL_ID);

    private final Map<RequesterBlockEntity, RequestTracker> byRequester = new IdentityHashMap<>();
    private final Long2ObjectOpenHashMap<RequestTracker> byId = new Long2ObjectOpenHashMap<>();

    private long inventorySerial = Long.MIN_VALUE;

    public RequesterTerminalMenu(int id, Inventory playerInventory, RequesterTerminalPart host) {
        super(TYPE, id, playerInventory, host);
        createPlayerInventorySlots(playerInventory);
    }

    @Override
    public void broadcastChanges() {
        if (isClientSide()) return;
        super.broadcastChanges();

        IGrid grid = getGrid();
        VisitorState state = new VisitorState();
        if (grid != null) visitRequesters(grid, state);

        if (state.forceFullUpdate || state.total != byRequester.size()) {
            sendFullUpdate(grid);
        } else {
            sendPartialUpdate();
        }
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        RequestTracker requests = byId.get(id);
        if (requests == null) return;
        if (slot < 0 || slot >= requests.server.size()) {
            MERequester.LOGGER.warn("Client refers to invalid slot {} of {}", slot, requests.name.getString());
            return;
        }

        var request = requests.server.getStackInSlot(slot);
        var patternSlot = requests.server.getSlotInv(slot);
        var carried = getCarried();

        switch (action) {
            case PICKUP_OR_SET_DOWN:
                patternSlot.setItemDirect(0, carried.isEmpty() ? ItemStack.EMPTY : carried.copy());
                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (carried.isEmpty()) {
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                } else {
                    var copy = carried.copy();
                    copy.setCount(1);
                    patternSlot.setItemDirect(0, copy);
                }
                break;
            case SHIFT_CLICK:
                patternSlot.setItemDirect(0, ItemStack.EMPTY);
                break;
            case CREATIVE_DUPLICATE:
                if (player.getAbilities().instabuild && carried.isEmpty()) {
                    if (request.isEmpty()) {
                        setCarried(ItemStack.EMPTY);
                    } else {
                        var stack = request.copy();
                        stack.setCount(stack.getMaxStackSize());
                        setCarried(stack);
                    }
                }
                break;
            default:
        }
    }

    @Override
    protected ItemStack transferStackToMenu(ItemStack stack) {
        var requesters = byRequester.keySet()
            .stream().sorted(Comparator.comparingLong(RequesterBlockEntity::getSortValue)).toList();

        for (var requester : requesters) {
            var targetSlot = requester.getRequests().firstAvailableSlot();
            if (targetSlot == -1) continue;
            byRequester.get(requester).server.insertItem(targetSlot, stack, false);
            return stack;
        }
        return stack;
    }

    private void visitRequesters(IGrid grid, VisitorState state) {
        for (var requester : grid.getActiveMachines(RequesterBlockEntity.class)) {
            RequestTracker requestTracker = byRequester.get(requester);
            if (requestTracker == null || !requestTracker.name.equals(requester.getTermName())) {
                state.forceFullUpdate = true;
            }
            state.total++;
        }
    }

    private void sendFullUpdate(@Nullable IGrid grid) {
        // clear caches and existing data on the client
        byId.clear();
        byRequester.clear();
        sendClientPacket(RequesterTerminalPacket.clearExistingData());

        if (grid == null) return;

        for (var requester : grid.getActiveMachines(RequesterBlockEntity.class)) {
            byRequester.put(requester, new RequestTracker(requester));
        }

        for (var requestTracker : byRequester.values()) {
            byId.put(requestTracker.serverId, requestTracker);

            var server = requestTracker.server;
            var client = requestTracker.client;

            // get the data from the server requests
            var tag = server.serializeNBT();
            // synchronize the client data for later difference checks on partial updates
            client.deserializeNBT(tag);

            tag.putLong("sortBy", requestTracker.sortBy);
            tag.putString("un", Component.Serializer.toJson(requestTracker.name));
            sendClientPacket(RequesterTerminalPacket.inventory(requestTracker.serverId, tag));
        }
    }

    private void sendPartialUpdate() {
        for (var requestTracker : byRequester.values()) {
            var server = requestTracker.server;
            var client = requestTracker.client;

            CompoundTag tag = null;
            for (var i = 0; i < server.size(); i++) {
                var serverRequest = server.get(i);
                var clientRequest = client.get(i);
                if (serverRequest.isDifferent(clientRequest)) {
                    if (tag == null) {
                        tag = new CompoundTag();
                        tag.putLong("sortBy", requestTracker.sortBy);
                        tag.putString("un", Component.Serializer.toJson(requestTracker.name));
                    }
                    var serverData = serverRequest.serializeNBT();
                    tag.put(String.valueOf(i), serverData);
                    clientRequest.deserializeNBT(serverData);
                }
            }

            if (tag != null) {
                sendClientPacket(RequesterTerminalPacket.inventory(requestTracker.serverId, tag));
            }
        }
    }

    @Nullable
    private IGrid getGrid() {
        IActionHost host = getActionHost();
        if (host != null) {
            IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                return agn.getGrid();
            }
        }
        return null;
    }

    private void sendClientPacket(ServerToClientPacket<?> packet) {
        if (getPlayer() instanceof ServerPlayer serverPlayer) {
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), packet);
        }
    }

    private static class VisitorState {
        private int total;
        private boolean forceFullUpdate;
    }

    private final class RequestTracker {

        private final long serverId = inventorySerial++;
        private final long sortBy;
        private final Component name;
        private final Requests server;
        private final Requests client;

        private RequestTracker(RequesterBlockEntity requester) {
            this.sortBy = requester.getSortValue();
            this.name = requester.getTermName();
            this.server = requester.getRequests();
            this.client = new Requests();
        }
    }
}
