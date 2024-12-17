package net.sxnu.autoclicker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class OptionsScreen extends Screen {
    private final HashMap<ButtonWidget, String> buttonTooltips = new HashMap<>();
    private final HashMap<TextFieldWidget, String> sliderTooltips = new HashMap<>();
    private TextFieldWidget leftHoldingSpamSpeed;
    private TextFieldWidget rightHoldingSpamSpeed;

    protected OptionsScreen() {
        super(Text.empty());
    }

    public static Screen createScreen(@Nullable Screen parent) {
        return new OptionsScreen();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2; // Screen center X
        int centerY = this.height / 2; // Screen center Y
        int elementWidth = 130; // Standard element width
        int elementHeight = 20; // Standard element height
        int verticalSpacing = 22; // Vertical spacing between elements
        int horizontalSpacing = 20; // Horizontal spacing between elements

        // Add sliders for left and right holding spam speeds
        OptionsSliderWidget leftHoldingSpamSpeedSlider = new OptionsSliderWidget(
                centerX - (elementWidth + horizontalSpacing / 2), centerY - 50, elementWidth, elementHeight,
                Text.of("Left Spam Speed"),
                AutoClicker.leftHolding.getSpeed() / 1200.0, // Normalize value to 0.0-1.0
                value -> {
                    AutoClicker.leftHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );

        OptionsSliderWidget rightHoldingSpamSpeedSlider = new OptionsSliderWidget(
                centerX + (horizontalSpacing / 2), centerY - 50, elementWidth, elementHeight,
                Text.of("Right Spam Speed"),
                AutoClicker.rightHolding.getSpeed() / 1200.0, // Normalize value to 0.0-1.0
                value -> {
                    AutoClicker.rightHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );

        this.addDrawableChild(leftHoldingSpamSpeedSlider);
        this.addDrawableChild(rightHoldingSpamSpeedSlider);

        // Dynamically position buttons
        addCenteredButton(centerX - (elementWidth + horizontalSpacing / 2), centerY - 94, elementWidth, elementHeight,
                Language.GUI_ACTIVE.getText(AutoClicker.leftHolding.isActive()),
                button -> toggleActive(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.active");

        addCenteredButton(centerX + (horizontalSpacing / 2), centerY - 94, elementWidth, elementHeight,
                Language.GUI_ACTIVE.getText(AutoClicker.rightHolding.isActive()),
                button -> toggleActive(button, AutoClicker.rightHolding),
                "autoclicker-fabric.gui.help.active");

        addCenteredButton(centerX - (elementWidth + horizontalSpacing / 2), centerY - 72, elementWidth, elementHeight,
                Language.GUI_SPAMMING.getText(AutoClicker.leftHolding.isSpamming()),
                button -> toggleSpamming(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.spamming");

        addCenteredButton(centerX + (horizontalSpacing / 2), centerY - 72, elementWidth, elementHeight,
                Language.GUI_SPAMMING.getText(AutoClicker.rightHolding.isSpamming()),
                button -> toggleSpamming(button, (Holding.AttackHolding) AutoClicker.rightHolding),
                "autoclicker-fabric.gui.help.spamming");

        addCenteredButton(centerX - (elementWidth + horizontalSpacing / 2), centerY - 28, elementWidth, elementHeight,
                Language.GUI_RESPECT_COOLDOWN.getText(AutoClicker.leftHolding.isRespectCooldown()),
                button -> toggleCooldown(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.cooldown");

        addCenteredButton(centerX - (elementWidth + horizontalSpacing / 2), centerY - 6, elementWidth, elementHeight,
                Language.GUI_RESPECT_SHIELD.getText(AutoClicker.leftHolding.isRespectShield()),
                button -> toggleShield(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.shield");

        addCenteredButton(centerX - (elementWidth + horizontalSpacing / 2), centerY + 16, elementWidth, elementHeight,
                Language.GUI_MOB_MODE.getText(AutoClicker.leftHolding.isMobMode()),
                button -> toggleMobMode(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.mob-mode");

        addCenteredButton(centerX - elementWidth / 2, centerY + 38, elementWidth, elementHeight,
                Language.GUI_HUD_ENABLED.getText(AutoClicker.hudConfig.isEnabled()),
                button -> toggleHudEnabled(button),
                "autoclicker-fabric.gui.help.hud-enabled");

    }

    private void updateSpamSpeed(String s, TextFieldWidget widget, Holding setting) {
        if (s.startsWith("0") && s.length() > 1) {
            s = s.substring(1);
            widget.setText(s);
        }

        try {
            setting.setSpeed(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            setting.setSpeed(0);
            widget.setText(String.valueOf(setting.getSpeed()));
        }

        AutoClicker.getInstance().saveConfig();
    }

    private void addCenteredButton(int x, int y, int width, int height, Text message, ButtonWidget.PressAction onPress, String tooltipKey) {
        ButtonWidget button = ButtonWidget.builder(message, onPress)
                .dimensions(x, y, width, height)
                .build();
        this.buttonTooltips.put(this.addDrawableChild(button), tooltipKey);
    }

    private void toggleActive(ButtonWidget button, Holding setting) {
        setting.setActive(!setting.isActive());
        button.setMessage(Language.GUI_ACTIVE.getText(setting.isActive()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleSpamming(ButtonWidget button, Holding.AttackHolding setting) {
        setting.setSpamming(!setting.isSpamming());
        button.setMessage(Language.GUI_SPAMMING.getText(setting.isSpamming()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleCooldown(ButtonWidget button, Holding.AttackHolding setting) {
        setting.setRespectCooldown(!setting.isRespectCooldown());
        button.setMessage(Language.GUI_RESPECT_COOLDOWN.getText(setting.isRespectCooldown()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleShield(ButtonWidget button, Holding.AttackHolding setting) {
        setting.setRespectShield(!setting.isRespectShield());
        button.setMessage(Language.GUI_RESPECT_SHIELD.getText(setting.isRespectShield()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleMobMode(ButtonWidget button, Holding.AttackHolding setting) {
        setting.setMobMode(!setting.isMobMode());
        button.setMessage(Language.GUI_MOB_MODE.getText(setting.isMobMode()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleHudEnabled(ButtonWidget button) {
        AutoClicker.hudConfig.setEnabled(!AutoClicker.hudConfig.isEnabled());
        button.setMessage(Language.GUI_HUD_ENABLED.getText(AutoClicker.hudConfig.isEnabled()));
        AutoClicker.getInstance().saveConfig();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2; // Screen center X
        int labelY = 10; // Fixed Y position for the labels
        int elementWidth = 130; // Width of text fields
        int horizontalSpacing = 20; // Spacing between text fields

        // Calculate positions for left and right labels
        int leftFieldX = centerX - (elementWidth + horizontalSpacing / 2);
        int rightFieldX = centerX + (horizontalSpacing / 2);

        // Render labels at a fixed Y position
        context.drawTextWithShadow(
                this.textRenderer, Language.GUI_ATTACK.getText().asOrderedText(),
                leftFieldX + elementWidth / 2 - this.textRenderer.getWidth(Language.GUI_ATTACK.getText()) / 2,
                labelY, // Fixed Y position for the label
                0xFFFFFF
        );

        context.drawTextWithShadow(
                this.textRenderer, Language.GUI_USE.getText().asOrderedText(),
                rightFieldX + elementWidth / 2 - this.textRenderer.getWidth(Language.GUI_USE.getText()) / 2,
                labelY, // Fixed Y position for the label
                0xFFFFFF
        );

        // Render tooltips for buttons
        for (ButtonWidget button : buttonTooltips.keySet()) {
            if (button.isHovered()) {
                this.renderHelpingTip(context, Text.translatable(this.buttonTooltips.get(button)), mouseX, mouseY);
            }
        }

        // Render tooltips for sliders
        for (TextFieldWidget widget : sliderTooltips.keySet()) {
            if (widget.isHovered()) {
                this.renderHelpingTip(context, Text.translatable(this.sliderTooltips.get(widget)), mouseX, mouseY);
            }
        }
    }






    private void renderHelpingTip(DrawContext context, Text text, int mouseX, int mouseY) {
        context.drawTooltip(
                this.textRenderer, this.textRenderer.wrapLines(StringVisitable.plain(text.getString()), 250), HoveredTooltipPositioner.INSTANCE, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == AutoClicker.openConfig.getDefaultKey().getCode()) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
