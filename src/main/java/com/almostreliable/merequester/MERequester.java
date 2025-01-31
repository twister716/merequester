package com.almostreliable.merequester;

import com.almostreliable.merequester.data.MERequesterData;
import com.almostreliable.merequester.network.PacketHandler;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String REQUESTER_ID = "requester";
    public static final String TERMINAL_ID = "requester_terminal";

    public MERequester(IEventBus modEventBus, ModContainer modContainer) {
        Registration.BLOCKS.register(modEventBus);
        Registration.ITEMS.register(modEventBus);
        Registration.BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(Registration::registerContents);
        modEventBus.addListener(Registration::registerCapabilities);
        modEventBus.addListener(Registration.Tab::initContents);
        modEventBus.addListener(PacketHandler::onPacketRegistration);
        MERequesterData.DR.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }
}
