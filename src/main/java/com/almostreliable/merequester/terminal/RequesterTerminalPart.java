package com.almostreliable.merequester.terminal;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import com.almostreliable.merequester.MERequester;
import com.almostreliable.merequester.Utils;

import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractDisplayPart;
import appeng.parts.reporting.PatternAccessTerminalPart;

/**
 * yoinked from {@link PatternAccessTerminalPart}
 */
public class RequesterTerminalPart extends AbstractDisplayPart {

    @PartModels
    private static final ResourceLocation MODEL_OFF = Utils.getRL(String.format("part/%s_off", MERequester.TERMINAL_ID));
    @PartModels
    private static final ResourceLocation MODEL_ON = Utils.getRL(String.format("part/%s_on", MERequester.TERMINAL_ID));

    private static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    private static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    private static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public RequesterTerminalPart(IPartItem<?> partItem) {
        super(partItem, true);
    }

    @Override
    public boolean onUseWithoutItem(Player player, Vec3 pos) {
        if (!super.onUseWithoutItem(player, pos) && !isClientSide()) {
            MenuOpener.open(RequesterTerminalMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    @Override
    public IPartModel getStaticModels() {
        return selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }
}
