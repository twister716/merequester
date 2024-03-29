package com.almostreliable.merequester;

import appeng.api.util.AEColor;
import appeng.client.render.StaticItemColor;
import appeng.init.client.InitScreens;
import com.almostreliable.merequester.client.RequesterScreen;
import com.almostreliable.merequester.client.RequesterTerminalScreen;
import com.almostreliable.merequester.network.PacketHandler;
import com.almostreliable.merequester.requester.RequesterMenu;
import com.almostreliable.merequester.terminal.RequesterTerminalMenu;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;

@Mod(BuildConfig.MOD_ID)
public final class MERequester {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String REQUESTER_ID = "requester";
    public static final String TERMINAL_ID = "requester_terminal";

    public MERequester(IEventBus modEventBus) {
        modEventBus.addListener(Registration::registerContents);
        modEventBus.addListener(Registration::registerCapabilities);
        modEventBus.addListener(Registration.Tab::initContents);
        modEventBus.addListener(PacketHandler::onPacketRegistration);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(MERequesterClient::registerScreens);
            modEventBus.addListener(MERequesterClient::registerColors);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
    }

    private static class MERequesterClient {

        @SuppressWarnings("RedundantTypeArguments")
        private static void registerScreens(RegisterMenuScreensEvent event) {
            InitScreens.register(RequesterMenu.TYPE, RequesterScreen::new, String.format("/screens/%s.json", REQUESTER_ID));
            InitScreens.<RequesterTerminalMenu, RequesterTerminalScreen<RequesterTerminalMenu>> register(
                RequesterTerminalMenu.TYPE,
                RequesterTerminalScreen::new,
                String.format("/screens/%s.json", TERMINAL_ID)
            );
        }

        private static void registerColors(RegisterColorHandlersEvent.Item event) {
            event.register(new StaticItemColor(AEColor.TRANSPARENT), Registration.REQUESTER_TERMINAL);
        }
    }
}
