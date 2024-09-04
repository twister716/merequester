package com.almostreliable.merequester;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.almostreliable.merequester.data.MERequesterRequest;
import com.almostreliable.merequester.requester.RequesterBlock;
import com.almostreliable.merequester.requester.RequesterBlockEntity;
import com.almostreliable.merequester.requester.RequesterMenu;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import com.almostreliable.merequester.terminal.RequesterTerminalPart;

import appeng.api.AECapabilities;
import appeng.api.parts.PartModels;
import appeng.block.AEBaseBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;

import static com.almostreliable.merequester.MERequester.REQUESTER_ID;
import static com.almostreliable.merequester.MERequester.TERMINAL_ID;

public final class Registration {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BuildConfig.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BuildConfig.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE,
        BuildConfig.MOD_ID
    );
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
        Registries.MENU,
        BuildConfig.MOD_ID
    );
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(BuildConfig.MOD_ID);

    public static final DeferredBlock<RequesterBlock> REQUESTER_BLOCK = BLOCKS.registerBlock(
        REQUESTER_ID,
        RequesterBlock::new,
        AEBaseBlock.metalProps()
    );
    public static final DeferredItem<BlockItem> REQUESTER_ITEM = ITEMS.registerSimpleBlockItem(
        REQUESTER_ID,
        REQUESTER_BLOCK,
        new Item.Properties()
    );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RequesterBlockEntity>> REQUESTER_ENTITY
        = BLOCK_ENTITIES.register(REQUESTER_ID, () -> {
        // noinspection DataFlowIssue
        var type = BlockEntityType.Builder
            .of(RequesterBlockEntity::new, REQUESTER_BLOCK.get())
            .build(null);
        AEBaseBlockEntity.registerBlockEntityItem(type, REQUESTER_BLOCK.asItem());
        REQUESTER_BLOCK.get().setBlockEntity(RequesterBlockEntity.class, type, null, null);
        return type;
    });
    public static final DeferredHolder<MenuType<?>, MenuType<RequesterMenu>> REQUESTER_MENU = MENUS.register(
        REQUESTER_ID,
        () -> RequesterMenu.TYPE
    );

    public static final DeferredItem<PartItem<RequesterTerminalPart>> REQUESTER_TERMINAL = ITEMS.registerItem(
        TERMINAL_ID,
        properties -> {
            PartModels.registerModels(PartModelsHelper.createModels(RequesterTerminalPart.class));

            return new PartItem<>(
                properties,
                RequesterTerminalPart.class,
                RequesterTerminalPart::new
            );
        }
    );
    public static final DeferredHolder<MenuType<?>, MenuType<RequesterTerminalMenu>> REQUESTER_TERMINAL_MENU = MENUS.register(
        TERMINAL_ID,
        () -> RequesterTerminalMenu.TYPE
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<MERequesterRequest>>> EXPORTED_REQUESTS =
        COMPONENTS.register(
            "exported_requests",
            () -> DataComponentType.<List<MERequesterRequest>> builder()
                .persistent(MERequesterRequest.CODEC.listOf())
                .networkSynchronized(MERequesterRequest.STREAM_CODEC.apply(ByteBufCodecs.list()))
                .build()
        );

    private Registration() {}

    static void init(IEventBus modEventBus) {
        modEventBus.addListener(Registration::registerContents);
        modEventBus.addListener(Registration::registerCapabilities);
        modEventBus.addListener(Tab::initContents);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
        COMPONENTS.register(modEventBus);
    }

    private static void registerContents(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
            Tab.registerTab(event);
        }
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            AECapabilities.IN_WORLD_GRID_NODE_HOST,
            REQUESTER_ENTITY.get(),
            (requester, ctx) -> requester
        );
    }

    public static final class Tab {

        public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Utils.getRL("tab"));
        private static final CreativeModeTab TAB = CreativeModeTab.builder()
            .title(Utils.translate("itemGroup", "tab"))
            .icon(REQUESTER_BLOCK::toStack)
            .noScrollBar()
            .build();

        private Tab() {}

        private static void initContents(BuildCreativeModeTabContentsEvent event) {
            if (event.getTabKey() == TAB_KEY) {
                event.accept(REQUESTER_BLOCK);
                event.accept(REQUESTER_TERMINAL);
            }
        }

        private static void registerTab(RegisterEvent registerEvent) {
            registerEvent.register(Registries.CREATIVE_MODE_TAB, TAB_KEY.location(), () -> TAB);
        }
    }
}
