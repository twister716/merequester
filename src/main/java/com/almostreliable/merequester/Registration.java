package com.almostreliable.merequester;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;

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

    static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENUS.register(modEventBus);
    }

    private Registration() {}

    static void registerContents(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.CREATIVE_MODE_TAB) {
            Tab.registerTab(event);
        }
    }

    static void registerCapabilities(RegisterCapabilitiesEvent event) {
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

        static void initContents(BuildCreativeModeTabContentsEvent event) {
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
