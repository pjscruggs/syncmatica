package ch.endte.syncmatica.litematica_mixin;

import ch.endte.syncmatica.Context;
import ch.endte.syncmatica.litematica.LitematicManager;
import ch.endte.syncmatica.litematica.gui.ButtonListenerChangeMenu;
import ch.endte.syncmatica.litematica.gui.IGuiBase;
import ch.endte.syncmatica.litematica.gui.MainMenuButtonType;
import fi.dy.masa.litematica.gui.GuiMainMenu;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu extends GuiBase {

    @Inject(method = "initGui", at = @At("RETURN"), remap = false)
    public void initGui(final CallbackInfo ci) {
        ButtonBase lastButton = null;
        for (final ButtonBase button : ((IGuiBase) this).getButtons()) {
            if (button.getY() == 30 && (lastButton == null || button.getX() > lastButton.getX())) {
                lastButton = button;
            }
        }
        if (lastButton == null) {
            return;
        }

        final int width = Math.max(lastButton.getWidth(), getSyncmaticaButtonWidth());
        int x = lastButton.getX() + lastButton.getWidth() + 20;
        int y = lastButton.getY();
        if (x + width > this.width - 12) {
            x = lastButton.getX();
            y += 22;
        }
        final int buttonWidth = Math.min(width, Math.max(1, this.width - x - 12));
        createChangeMenuButton(x, y, buttonWidth, MainMenuButtonType.VIEW_SYNCMATICS);
        y += 22;
        createChangeMenuButton(x, y, buttonWidth, MainMenuButtonType.MATERIAL_GATHERINGS).setEnabled(false);
    }

    private ButtonGeneric createChangeMenuButton(final int x, final int y, final int width, final MainMenuButtonType type) {
        final String label = type.getTranslatedKey();
        String displayLabel = label;
        if (getStringWidth(label) > width - 10) {
            while (!displayLabel.isEmpty() && getStringWidth(displayLabel + "...") > width - 10) {
                displayLabel = displayLabel.substring(0, displayLabel.offsetByCodePoints(displayLabel.length(), -1));
            }
            displayLabel = displayLabel.isEmpty() ? "" : displayLabel + "...";
        }
        final ButtonGeneric button = new ButtonGeneric(x, y, width, 20, displayLabel, type.getIcon());
        if (!displayLabel.equals(label)) {
            button.setHoverStrings(label);
        }
        final Context con = LitematicManager.getInstance().getActiveContext();
        button.setEnabled(con != null && con.isStarted());
        addButton(button, new ButtonListenerChangeMenu(type, this));
        return button;
    }

    @Unique
    private int getSyncmaticaButtonWidth() {
        int width = 0;

        for (final MainMenuButtonType type : MainMenuButtonType.values()) {
            width = Math.max(width, getStringWidth(type.getTranslatedKey()) + 30);
        }


        return width;
    }
}
