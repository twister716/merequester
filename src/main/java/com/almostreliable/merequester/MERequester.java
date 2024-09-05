package com.almostreliable.merequester;

import com.almostreliable.merequester.core.Config;
import com.almostreliable.merequester.core.Registration;
import com.almostreliable.merequester.network.PacketHandler;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ModConstants.MOD_ID)
public final class MERequester {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String REQUESTER_ID = "requester";
    public static final String TERMINAL_ID = "requester_terminal";

    public MERequester(IEventBus modEventBus, ModContainer modContainer) {
        Registration.init(modEventBus);
        PacketHandler.init(modEventBus);
        Config.init(modContainer);
    }
}
