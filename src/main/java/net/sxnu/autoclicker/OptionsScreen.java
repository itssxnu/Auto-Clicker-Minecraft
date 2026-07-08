package net.sxnu.autoclicker;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class OptionsScreen extends Screen {
    private final HashMap<Button, String> buttonTooltips = new HashMap<>();
    private final HashMap<EditBox, String> sliderTooltips = new HashMap<>();

    protected OptionsScreen() {
        super(Component.empty());
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
                Component.nullToEmpty("Left Spam Speed"),
                AutoClicker.leftHolding.getSpeed() / 1200.0,
                value -> {
                    AutoClicker.leftHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );
        this.addRenderableWidget(leftHoldingSpamSpeedSlider);

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
                Language.GUI_TARGET_MODE.getText(Component.translatable(AutoClicker.leftHolding.getTargetMode().getTranslationKey())),
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
                Component.nullToEmpty("Right Spam Speed"),
                AutoClicker.rightHolding.getSpeed() / 1200.0,
                value -> {
                    AutoClicker.rightHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );
        this.addRenderableWidget(rightHoldingSpamSpeedSlider);

        addCenteredButton(rightColX, centerY - 28, elementWidth, elementHeight,
                Language.GUI_AUTO_BRIDGE.getText(activeProfile.isAutoBridge()),
                button -> toggleAutoBridge(button, activeProfile),
                "autoclicker-fabric.gui.help.auto-bridge");
    }

    private void addCenteredButton(int x, int y, int width, int height, Component message, Button.OnPress onPress, String tooltipKey) {
        Button button = Button.builder(message, onPress)
                .bounds(x, y, width, height)
                .build();
        this.buttonTooltips.put(this.addRenderableWidget(button), tooltipKey);
    }

    private void toggleActive(Button button, Holding setting) {
        setting.setActive(!setting.isActive());
        button.setMessage(Language.GUI_ACTIVE.getText(setting.isActive()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleSpamming(Button button, Holding setting) {
        setting.setSpamming(!setting.isSpamming());
        button.setMessage(Language.GUI_SPAMMING.getText(setting.isSpamming()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleCooldown(Button button, Holding.AttackHolding setting) {
        setting.setRespectCooldown(!setting.isRespectCooldown());
        button.setMessage(Language.GUI_RESPECT_COOLDOWN.getText(setting.isRespectCooldown()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleShield(Button button, Holding.AttackHolding setting) {
        setting.setRespectShield(!setting.isRespectShield());
        button.setMessage(Language.GUI_RESPECT_SHIELD.getText(setting.isRespectShield()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleMobMode(Button button, Holding.AttackHolding setting) {
        setting.setMobMode(!setting.isMobMode());
        button.setMessage(Language.GUI_MOB_MODE.getText(setting.isMobMode()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleTargetMode(Button button, Holding.AttackHolding setting) {
        TargetMode current = setting.getTargetMode();
        TargetMode next = TargetMode.values()[(current.ordinal() + 1) % TargetMode.values().length];
        setting.setTargetMode(next);
        button.setMessage(Language.GUI_TARGET_MODE.getText(Component.translatable(next.getTranslationKey())));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleHudEnabled(Button button) {
        AutoClicker.hudConfig.setEnabled(!AutoClicker.hudConfig.isEnabled());
        button.setMessage(Language.GUI_HUD_ENABLED.getText(AutoClicker.hudConfig.isEnabled()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleProfile(Button button) {
        Config config = AutoClicker.getInstance().getConfig();
        String current = config.getActiveProfile();
        java.util.List<String> profileNames = new java.util.ArrayList<>(config.getProfiles().keySet());
        int index = profileNames.indexOf(current);
        String next = profileNames.get((index + 1) % profileNames.size());
        config.setActiveProfile(next);

        // Reconnect settings to current profile clicks
        AutoClicker.leftHolding = new Holding.AttackHolding(this.minecraft.options.keyAttack, config.getLeftClick());
        AutoClicker.rightHolding = new Holding(this.minecraft.options.keyUse, config.getRightClick());

        AutoClicker.getInstance().saveConfig();

        // Refresh UI screen
        this.init(this.width, this.height);
    }

    private void toggleAutoEat(Button button, Config.ProfileConfig activeProfile) {
        activeProfile.setAutoEat(!activeProfile.isAutoEat());
        button.setMessage(Language.GUI_AUTO_EAT.getText(activeProfile.isAutoEat()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleAutoHeal(Button button, Config.ProfileConfig activeProfile) {
        activeProfile.setAutoHeal(!activeProfile.isAutoHeal());
        button.setMessage(Language.GUI_AUTO_HEAL.getText(activeProfile.isAutoHeal()));
        AutoClicker.getInstance().saveConfig();
    }

    private void toggleAutoBridge(Button button, Config.ProfileConfig activeProfile) {
        activeProfile.setAutoBridge(!activeProfile.isAutoBridge());
        button.setMessage(Language.GUI_AUTO_BRIDGE.getText(activeProfile.isAutoBridge()));
        AutoClicker.getInstance().saveConfig();
    }


    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        this.extractBackground(context, mouseX, mouseY, delta);
        super.extractRenderState(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int labelY = 10;
        int elementWidth = 100;

        int leftColX = centerX - 160;
        int midColX = centerX - 50;
        int rightColX = centerX + 60;

        // Left Column Label
        Component leftLabel = Language.GUI_ATTACK.getText();
        context.text(
                this.font, leftLabel.getVisualOrderText(),
                leftColX + elementWidth / 2 - this.font.width(leftLabel) / 2,
                labelY,
                0xFFFFFF,
                true
        );

        // Middle Column Label
        Component midLabel = Component.nullToEmpty("Global & HUD");
        context.text(
                this.font, midLabel.getVisualOrderText(),
                midColX + elementWidth / 2 - this.font.width(midLabel) / 2,
                labelY,
                0xFFFFFF,
                true
        );

        // Right Column Label
        Component rightLabel = Language.GUI_USE.getText();
        context.text(
                this.font, rightLabel.getVisualOrderText(),
                rightColX + elementWidth / 2 - this.font.width(rightLabel) / 2,
                labelY,
                0xFFFFFF,
                true
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

            context.text(this.font, Component.nullToEmpty("Mod Features Guide:"), leftLimit, guideY, titleColor, true);
            guideY += 12;

            String warningText = "Make sure to toggle the autoclicker on (default: 'I') for these to work.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(warningText), availableWidth)) {
                context.text(this.font, line, leftLimit, guideY, 0xFF5555, true);
                guideY += 9;
            }
            guideY += 4;

            context.text(this.font, Component.nullToEmpty("• Auto-Bridge:"), leftLimit, guideY, bulletColor, true);
            guideY += 9;
            String bridgeText = "Sneaks on edges & places blocks. Automatically swaps matching blocks. Keeps you crouched if out of blocks.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(bridgeText), availableWidth - 6)) {
                context.text(this.font, line, leftLimit + 6, guideY, color, true);
                guideY += 9;
            }
            guideY += 2;

            context.text(this.font, Component.nullToEmpty("• Auto-Eat:"), leftLimit, guideY, bulletColor, true);
            guideY += 9;
            String eatText = "Switches to hotbar food when hunger is 4 bars down (<= 12) and eats until full.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(eatText), availableWidth - 6)) {
                context.text(this.font, line, leftLimit + 6, guideY, color, true);
                guideY += 9;
            }
            guideY += 2;

            context.text(this.font, Component.nullToEmpty("• Auto-Heal:"), leftLimit, guideY, bulletColor, true);
            guideY += 9;
            String healText = "Uses Golden Apples or Healing Potions from hotbar when health <= 10.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(healText), availableWidth - 6)) {
                context.text(this.font, line, leftLimit + 6, guideY, color, true);
                guideY += 9;
            }
            guideY += 2;

            context.text(this.font, Component.nullToEmpty("• Target Mode:"), leftLimit, guideY, bulletColor, true);
            guideY += 9;
            String targetText = "Filters entity attacks. Villagers, golems, players, and pets are protected.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(targetText), availableWidth - 6)) {
                context.text(this.font, line, leftLimit + 6, guideY, color, true);
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

            context.text(this.font, Component.nullToEmpty("Target Modes Guide:"), rightColLeftLimit, guideY, titleColor, true);
            guideY += 12;

            context.text(this.font, Component.nullToEmpty("• All Mobs:"), rightColLeftLimit, guideY, bulletColor, true);
            guideY += 9;
            String allMobsText = "Attacks any target in your direct line of sight.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(allMobsText), rightColAvailableWidth - 6)) {
                context.text(this.font, line, rightColLeftLimit + 6, guideY, color, true);
                guideY += 9;
            }
            guideY += 2;

            context.text(this.font, Component.nullToEmpty("• Hostiles Only:"), rightColLeftLimit, guideY, bulletColor, true);
            guideY += 9;
            String hostilesText = "Attacks monsters (Zombies, Skeletons, Creepers, Phantoms, Slimes).";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(hostilesText), rightColAvailableWidth - 6)) {
                context.text(this.font, line, rightColLeftLimit + 6, guideY, color, true);
                guideY += 9;
            }
            guideY += 2;

            context.text(this.font, Component.nullToEmpty("• Passives Only:"), rightColLeftLimit, guideY, bulletColor, true);
            guideY += 9;
            String passivesText = "Attacks passive entities (Cows, Sheep, Squids, Fish, Bats).";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(passivesText), rightColAvailableWidth - 6)) {
                context.text(this.font, line, rightColLeftLimit + 6, guideY, color, true);
                guideY += 9;
            }
            guideY += 2;

            context.text(this.font, Component.nullToEmpty("• Hostiles & Passives:"), rightColLeftLimit, guideY, bulletColor, true);
            guideY += 9;
            String hostilesPassivesText = "Attacks monsters and animals, but excludes villagers, golems, pets, and players.";
            for (net.minecraft.util.FormattedCharSequence line : this.font.split(FormattedText.of(hostilesPassivesText), rightColAvailableWidth - 6)) {
                context.text(this.font, line, rightColLeftLimit + 6, guideY, color, true);
                guideY += 9;
            }
        }

        // Render tooltips for buttons
        for (Button button : buttonTooltips.keySet()) {
            if (button.isHovered()) {
                this.renderHelpingTip(context, Component.translatable(this.buttonTooltips.get(button)), mouseX, mouseY);
            }
        }

        // Render tooltips for sliders
        for (EditBox widget : sliderTooltips.keySet()) {
            if (widget.isHovered()) {
                this.renderHelpingTip(context, Component.translatable(this.sliderTooltips.get(widget)), mouseX, mouseY);
            }
        }
    }

    private void renderHelpingTip(GuiGraphicsExtractor context, Component text, int mouseX, int mouseY) {
        context.setTooltipForNextFrame(this.font, text, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == AutoClicker.openConfig.getDefaultKey().getValue()) {
            this.onClose();
            return true;
        }

        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
