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
import java.util.List;

public class OptionsScreen extends Screen {
    private final HashMap<ButtonWidget, String> buttonTooltips = new HashMap<>();
    private final HashMap<TextFieldWidget, String> sliderTooltips = new HashMap<>();

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
        int elementWidth = 100; // Element width for three-column layout
        int elementHeight = 20; // Standard element height

        // Clear tooltips
        this.buttonTooltips.clear();
        this.sliderTooltips.clear();

        // 3 Column Layout configuration
        int leftColX = centerX - 160;
        int midColX = centerX - 50;
        int rightColX = centerX + 60;

        Config.ProfileConfig activeProfile = AutoClicker.getInstance().getConfig().getActiveProfileConfig();
        if (activeProfile == null) return;

        // --- LEFT COLUMN (Attack / Left Click) ---
        addCenteredButton(leftColX, centerY - 94, elementWidth, elementHeight,
                Language.GUI_ACTIVE.getText(AutoClicker.leftHolding.isActive()),
                button -> toggleActive(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.active");

        addCenteredButton(leftColX, centerY - 72, elementWidth, elementHeight,
                Language.GUI_SPAMMING.getText(AutoClicker.leftHolding.isSpamming()),
                button -> toggleSpamming(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.spamming");

        OptionsSliderWidget leftHoldingSpamSpeedSlider = new OptionsSliderWidget(
                leftColX, centerY - 50, elementWidth, elementHeight,
                Text.of("Left Spam Speed"),
                AutoClicker.leftHolding.getSpeed() / 1200.0,
                value -> {
                    AutoClicker.leftHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );
        this.addDrawableChild(leftHoldingSpamSpeedSlider);

        addCenteredButton(leftColX, centerY - 28, elementWidth, elementHeight,
                Language.GUI_RESPECT_COOLDOWN.getText(AutoClicker.leftHolding.isRespectCooldown()),
                button -> toggleCooldown(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.cooldown");

        addCenteredButton(leftColX, centerY - 6, elementWidth, elementHeight,
                Language.GUI_RESPECT_SHIELD.getText(AutoClicker.leftHolding.isRespectShield()),
                button -> toggleShield(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.shield");

        addCenteredButton(leftColX, centerY + 16, elementWidth, elementHeight,
                Language.GUI_MOB_MODE.getText(AutoClicker.leftHolding.isMobMode()),
                button -> toggleMobMode(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.mob-mode");

        addCenteredButton(leftColX, centerY + 38, elementWidth, elementHeight,
                Language.GUI_TARGET_MODE.getText(Text.translatable(AutoClicker.leftHolding.getTargetMode().getTranslationKey())),
                button -> toggleTargetMode(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.target-mode");


        // --- MIDDLE COLUMN (Global / HUD Settings) ---
        addCenteredButton(midColX, centerY - 94, elementWidth, elementHeight,
                Language.GUI_PROFILE.getText(AutoClicker.getInstance().getConfig().getActiveProfile()),
                button -> toggleProfile(button),
                "autoclicker-fabric.gui.help.profile");

        addCenteredButton(midColX, centerY - 72, elementWidth, elementHeight,
                Language.GUI_AUTO_EAT.getText(activeProfile.isAutoEat()),
                button -> toggleAutoEat(button, activeProfile),
                "autoclicker-fabric.gui.help.auto-eat");

        addCenteredButton(midColX, centerY - 50, elementWidth, elementHeight,
                Language.GUI_AUTO_HEAL.getText(activeProfile.isAutoHeal()),
                button -> toggleAutoHeal(button, activeProfile),
                "autoclicker-fabric.gui.help.auto-heal");

        addCenteredButton(midColX, centerY - 28, elementWidth, elementHeight,
                Language.GUI_HUD_ENABLED.getText(AutoClicker.hudConfig.isEnabled()),
                button -> toggleHudEnabled(button),
                "autoclicker-fabric.gui.help.hud-enabled");

        // --- RIGHT COLUMN (Use / Right Click Settings) ---
        addCenteredButton(rightColX, centerY - 94, elementWidth, elementHeight,
                Language.GUI_ACTIVE.getText(AutoClicker.rightHolding.isActive()),
                button -> toggleActive(button, AutoClicker.rightHolding),
                "autoclicker-fabric.gui.help.active");

        addCenteredButton(rightColX, centerY - 72, elementWidth, elementHeight,
                Language.GUI_SPAMMING.getText(AutoClicker.rightHolding.isSpamming()),
                button -> toggleSpamming(button, AutoClicker.rightHolding),
                "autoclicker-fabric.gui.help.spamming");

        OptionsSliderWidget rightHoldingSpamSpeedSlider = new OptionsSliderWidget(
                rightColX, centerY - 50, elementWidth, elementHeight,
                Text.of("Right Spam Speed"),
                AutoClicker.rightHolding.getSpeed() / 1200.0,
                value -> {
                    AutoClicker.rightHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );
        this.addDrawableChild(rightHoldingSpamSpeedSlider);

        addCenteredButton(rightColX, centerY - 28, elementWidth, elementHeight,
                Language.GUI_AUTO_BRIDGE.getText(activeProfile.isAutoBridge()),
                button -> toggleAutoBridge(button, activeProfile),
                "autoclicker-fabric.gui.help.auto-bridge");
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

    private void toggleSpamming(ButtonWidget button, Holding setting) {
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

    private void toggleTargetMode(ButtonWidget button, Holding.AttackHolding setting) {
        TargetMode current = setting.getTargetMode();
        TargetMode next = TargetMode.values()[(current.ordinal() + 1) % TargetMode.values().length];
        setting.setTargetMode(next);
        button.setMessage(Language.GUI_TARGET_MODE.getText(Text.translatable(next.getTranslationKey())));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleHudEnabled(ButtonWidget button) {
        AutoClicker.hudConfig.setEnabled(!AutoClicker.hudConfig.isEnabled());
        button.setMessage(Language.GUI_HUD_ENABLED.getText(AutoClicker.hudConfig.isEnabled()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleProfile(ButtonWidget button) {
        Config config = AutoClicker.getInstance().getConfig();
        String current = config.getActiveProfile();
        java.util.List<String> profileNames = new java.util.ArrayList<>(config.getProfiles().keySet());
        int index = profileNames.indexOf(current);
        String next = profileNames.get((index + 1) % profileNames.size());
        config.setActiveProfile(next);

        // Reconnect settings to current profile clicks
        AutoClicker.leftHolding = new Holding.AttackHolding(this.client.options.attackKey, config.getLeftClick());
        AutoClicker.rightHolding = new Holding(this.client.options.useKey, config.getRightClick());

        AutoClicker.getInstance().saveConfig();

        // Refresh UI screen
        this.init(this.client, this.width, this.height);
    }

    private void toggleAutoEat(ButtonWidget button, Config.ProfileConfig activeProfile) {
        activeProfile.setAutoEat(!activeProfile.isAutoEat());
        button.setMessage(Language.GUI_AUTO_EAT.getText(activeProfile.isAutoEat()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleAutoHeal(ButtonWidget button, Config.ProfileConfig activeProfile) {
        activeProfile.setAutoHeal(!activeProfile.isAutoHeal());
        button.setMessage(Language.GUI_AUTO_HEAL.getText(activeProfile.isAutoHeal()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleAutoBridge(ButtonWidget button, Config.ProfileConfig activeProfile) {
        activeProfile.setAutoBridge(!activeProfile.isAutoBridge());
        button.setMessage(Language.GUI_AUTO_BRIDGE.getText(activeProfile.isAutoBridge()));
        AutoClicker.getInstance().saveConfig();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int labelY = 10;
        int elementWidth = 100;

        int leftColX = centerX - 160;
        int midColX = centerX - 50;
        int rightColX = centerX + 60;

        // Left Column Label
        Text leftLabel = Language.GUI_ATTACK.getText();
        context.drawTextWithShadow(
                this.textRenderer, leftLabel.asOrderedText(),
                leftColX + elementWidth / 2 - this.textRenderer.getWidth(leftLabel) / 2,
                labelY,
                0xFFFFFF
        );

        // Middle Column Label
        Text midLabel = Text.of("Global & HUD");
        context.drawTextWithShadow(
                this.textRenderer, midLabel.asOrderedText(),
                midColX + elementWidth / 2 - this.textRenderer.getWidth(midLabel) / 2,
                labelY,
                0xFFFFFF
        );

        // Right Column Label
        Text rightLabel = Language.GUI_USE.getText();
        context.drawTextWithShadow(
                this.textRenderer, rightLabel.asOrderedText(),
                rightColX + elementWidth / 2 - this.textRenderer.getWidth(rightLabel) / 2,
                labelY,
                0xFFFFFF
        );

        // Render features and target filter guide on the left side with dynamic wrapping to prevent overlap
        int leftLimit = 15;
        int rightLimit = centerX - 175; // Padding before the left column
        int availableWidth = rightLimit - leftLimit;

        if (availableWidth >= 80) { // Only render if there is sufficient horizontal space
            int centerY = this.height / 2;
            int guideY = centerY - 104; // Start slightly higher
            int color = 0xAAAAAA; // Light gray for guide text
            int titleColor = 0xFFAA00; // Gold for headers
            int bulletColor = 0xFFFFFF; // White for bullets

            context.drawTextWithShadow(this.textRenderer, Text.of("Mod Features Guide:"), leftLimit, guideY, titleColor);
            guideY += 12;

            String warningText = "Make sure to toggle the autoclicker on (default: 'I') for these to work.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(warningText), availableWidth)) {
                context.drawTextWithShadow(this.textRenderer, line, leftLimit, guideY, 0xFF5555);
                guideY += 9;
            }
            guideY += 4;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Auto-Bridge:"), leftLimit, guideY, bulletColor);
            guideY += 9;
            String bridgeText = "Sneaks on edges & places blocks. Automatically swaps matching blocks. Keeps you crouched if out of blocks.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(bridgeText), availableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, leftLimit + 6, guideY, color);
                guideY += 9;
            }
            guideY += 2;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Auto-Eat:"), leftLimit, guideY, bulletColor);
            guideY += 9;
            String eatText = "Switches to hotbar food when hunger is 4 bars down (<= 12) and eats until full.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(eatText), availableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, leftLimit + 6, guideY, color);
                guideY += 9;
            }
            guideY += 2;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Auto-Heal:"), leftLimit, guideY, bulletColor);
            guideY += 9;
            String healText = "Uses Golden Apples or Healing Potions from hotbar when health <= 10.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(healText), availableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, leftLimit + 6, guideY, color);
                guideY += 9;
            }
            guideY += 2;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Target Mode:"), leftLimit, guideY, bulletColor);
            guideY += 9;
            String targetText = "Filters entity attacks. Villagers, golems, players, and pets are protected.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(targetText), availableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, leftLimit + 6, guideY, color);
                guideY += 9;
            }
        }

        // Render target modes guide on the right side with dynamic wrapping to prevent overlap
        int rightColLeftLimit = centerX + 175;
        int rightColRightLimit = this.width - 15;
        int rightColAvailableWidth = rightColRightLimit - rightColLeftLimit;

        if (rightColAvailableWidth >= 80) { // Only render if there is sufficient horizontal space
            int centerY = this.height / 2;
            int guideY = centerY - 104; // Start at the same height as the left column
            int color = 0xAAAAAA; // Light gray for guide text
            int titleColor = 0xFFAA00; // Gold for headers
            int bulletColor = 0xFFFFFF; // White for bullets

            context.drawTextWithShadow(this.textRenderer, Text.of("Target Modes Guide:"), rightColLeftLimit, guideY, titleColor);
            guideY += 12;

            context.drawTextWithShadow(this.textRenderer, Text.of("• All Mobs:"), rightColLeftLimit, guideY, bulletColor);
            guideY += 9;
            String allMobsText = "Attacks any target in your direct line of sight.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(allMobsText), rightColAvailableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, rightColLeftLimit + 6, guideY, color);
                guideY += 9;
            }
            guideY += 2;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Hostiles Only:"), rightColLeftLimit, guideY, bulletColor);
            guideY += 9;
            String hostilesText = "Attacks monsters (Zombies, Skeletons, Creepers, Phantoms, Slimes).";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(hostilesText), rightColAvailableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, rightColLeftLimit + 6, guideY, color);
                guideY += 9;
            }
            guideY += 2;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Passives Only:"), rightColLeftLimit, guideY, bulletColor);
            guideY += 9;
            String passivesText = "Attacks passive entities (Cows, Sheep, Squids, Fish, Bats).";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(passivesText), rightColAvailableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, rightColLeftLimit + 6, guideY, color);
                guideY += 9;
            }
            guideY += 2;

            context.drawTextWithShadow(this.textRenderer, Text.of("• Hostiles & Passives:"), rightColLeftLimit, guideY, bulletColor);
            guideY += 9;
            String hostilesPassivesText = "Attacks monsters and animals, but excludes villagers, golems, pets, and players.";
            for (net.minecraft.text.OrderedText line : this.textRenderer.wrapLines(StringVisitable.plain(hostilesPassivesText), rightColAvailableWidth - 6)) {
                context.drawTextWithShadow(this.textRenderer, line, rightColLeftLimit + 6, guideY, color);
                guideY += 9;
            }
        }

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
