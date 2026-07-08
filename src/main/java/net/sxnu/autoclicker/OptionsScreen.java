package net.sxnu.autoclicker;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.MultiLineTextWidget;
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
        int verticalGap = 2;
        int columnGap;

        if (this.width < 380) {
            columnGap = 4;
            elementWidth = (this.width - 32) / 3;
            elementWidth = Math.max(70, Math.min(100, elementWidth));
        } else {
            columnGap = 10;
        }

        if (this.height < 220) {
            verticalGap = 1;
            elementHeight = 18;
        }

        int totalMenuWidth = 3 * elementWidth + 2 * columnGap;
        int startX = centerX - totalMenuWidth / 2;

        int leftColX = startX;
        int midColX = startX + elementWidth + columnGap;
        int rightColX = startX + 2 * (elementWidth + columnGap);

        int rowHeight = elementHeight + verticalGap;
        int row0 = centerY - 4 * rowHeight - 6;
        int row1 = row0 + rowHeight;
        int row2 = row0 + 2 * rowHeight;
        int row3 = row0 + 3 * rowHeight;
        int row4 = row0 + 4 * rowHeight;
        int row5 = row0 + 5 * rowHeight;
        int row6 = row0 + 6 * rowHeight;
        int labelY = row0 - 15;

        // Clear tooltips
        this.buttonTooltips.clear();
        this.sliderTooltips.clear();

        Config.ProfileConfig activeProfile = AutoClicker.getInstance().getConfig().getActiveProfileConfig();
        if (activeProfile == null) return;

        // --- COLUMN LABELS ---
        StringWidget leftColLabelWidget = new StringWidget(
                leftColX, labelY, elementWidth, elementHeight,
                Language.GUI_ATTACK.getText().copy().withColor(0xFFFFFF),
                this.font
        );
        this.addRenderableWidget(leftColLabelWidget);

        StringWidget midColLabelWidget = new StringWidget(
                midColX, labelY, elementWidth, elementHeight,
                Component.literal("Global & HUD").withColor(0xFFFFFF),
                this.font
        );
        this.addRenderableWidget(midColLabelWidget);

        StringWidget rightColLabelWidget = new StringWidget(
                rightColX, labelY, elementWidth, elementHeight,
                Language.GUI_USE.getText().copy().withColor(0xFFFFFF),
                this.font
        );
        this.addRenderableWidget(rightColLabelWidget);

        // --- SIDE GUIDES ---
        int leftLimit = 15;
        int rightLimit = leftColX - 15;
        int availableWidth = rightLimit - leftLimit;

        if (availableWidth >= 80) {
            int guideY = centerY - 104;

            StringWidget titleWidget = new StringWidget(
                    leftLimit, guideY, availableWidth, 12,
                    Component.literal("Mod Features Guide:").withColor(0xFFAA00),
                    this.font
            );
            this.addRenderableWidget(titleWidget);
            guideY += 14;

            MultiLineTextWidget warningWidget = new MultiLineTextWidget(
                    leftLimit, guideY,
                    Component.literal("Make sure to toggle the autoclicker on (default: 'I') for these to work.").withColor(0xFF5555),
                    this.font
            );
            warningWidget.setMaxWidth(availableWidth);
            this.addRenderableWidget(warningWidget);
            guideY += warningWidget.getHeight() + 6;

            String detailsText = "• Auto-Bridge:\n" +
                    "Sneaks on edges & places blocks. Automatically swaps matching blocks. Keeps you crouched if out of blocks.\n\n" +
                    "• Auto-Eat:\n" +
                    "Switches to food when hunger <= 12 and eats until full.\n\n" +
                    "• Auto-Heal:\n" +
                    "Uses Golden Apples or Healing Potions from hotbar when health <= 10.";
            MultiLineTextWidget detailsWidget = new MultiLineTextWidget(
                    leftLimit, guideY,
                    Component.literal(detailsText).withColor(0xAAAAAA),
                    this.font
            );
            detailsWidget.setMaxWidth(availableWidth);
            this.addRenderableWidget(detailsWidget);
        }

        int rightColLeftLimit = rightColX + elementWidth + 15;
        int rightColRightLimit = this.width - 15;
        int rightColAvailableWidth = rightColRightLimit - rightColLeftLimit;

        if (rightColAvailableWidth >= 80) {
            int guideY = centerY - 104;

            StringWidget rightTitleWidget = new StringWidget(
                    rightColLeftLimit, guideY, rightColAvailableWidth, 12,
                    Component.literal("Target Modes Guide:").withColor(0xFFAA00),
                    this.font
            );
            this.addRenderableWidget(rightTitleWidget);
            guideY += 14;

            String targetText = "• All Mobs:\n" +
                    "Attacks any target in sight.\n\n" +
                    "• Hostiles Only:\n" +
                    "Attacks monsters (Zombies, Skeletons, Creepers, etc.).\n\n" +
                    "• Passives Only:\n" +
                    "Attacks passive animals (Cows, Sheep, Squids, etc.).\n\n" +
                    "• Hostiles & Passives:\n" +
                    "Attacks monsters and animals, but excludes villagers, golems, pets, and players.";
            MultiLineTextWidget rightDetailsWidget = new MultiLineTextWidget(
                    rightColLeftLimit, guideY,
                    Component.literal(targetText).withColor(0xAAAAAA),
                    this.font
            );
            rightDetailsWidget.setMaxWidth(rightColAvailableWidth);
            this.addRenderableWidget(rightDetailsWidget);
        }

        // --- LEFT COLUMN (Attack / Left Click) ---
        addCenteredButton(leftColX, row0, elementWidth, elementHeight,
                Language.GUI_ACTIVE.getText(AutoClicker.leftHolding.isActive()),
                button -> toggleActive(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.active");

        addCenteredButton(leftColX, row1, elementWidth, elementHeight,
                Language.GUI_SPAMMING.getText(AutoClicker.leftHolding.isSpamming()),
                button -> toggleSpamming(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.spamming");

        OptionsSliderWidget leftHoldingSpamSpeedSlider = new OptionsSliderWidget(
                leftColX, row2, elementWidth, elementHeight,
                Component.nullToEmpty("Left Spam Speed"),
                AutoClicker.leftHolding.getSpeed() / 1200.0,
                value -> {
                    AutoClicker.leftHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );
        this.addRenderableWidget(leftHoldingSpamSpeedSlider);

        addCenteredButton(leftColX, row3, elementWidth, elementHeight,
                Language.GUI_RESPECT_COOLDOWN.getText(AutoClicker.leftHolding.isRespectCooldown()),
                button -> toggleCooldown(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.cooldown");

        addCenteredButton(leftColX, row4, elementWidth, elementHeight,
                Language.GUI_RESPECT_SHIELD.getText(AutoClicker.leftHolding.isRespectShield()),
                button -> toggleShield(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.shield");

        addCenteredButton(leftColX, row5, elementWidth, elementHeight,
                Language.GUI_MOB_MODE.getText(AutoClicker.leftHolding.isMobMode()),
                button -> toggleMobMode(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.mob-mode");

        addCenteredButton(leftColX, row6, elementWidth, elementHeight,
                Language.GUI_TARGET_MODE.getText(Component.translatable(AutoClicker.leftHolding.getTargetMode().getTranslationKey())),
                button -> toggleTargetMode(button, AutoClicker.leftHolding),
                "autoclicker-fabric.gui.help.target-mode");


        // --- MIDDLE COLUMN (Global / HUD Settings) ---
        addCenteredButton(midColX, row0, elementWidth, elementHeight,
                Language.GUI_PROFILE.getText(AutoClicker.getInstance().getConfig().getActiveProfile()),
                button -> toggleProfile(button),
                "autoclicker-fabric.gui.help.profile");

        addCenteredButton(midColX, row1, elementWidth, elementHeight,
                Language.GUI_AUTO_EAT.getText(activeProfile.isAutoEat()),
                button -> toggleAutoEat(button, activeProfile),
                "autoclicker-fabric.gui.help.auto-eat");

        addCenteredButton(midColX, row2, elementWidth, elementHeight,
                Language.GUI_AUTO_HEAL.getText(activeProfile.isAutoHeal()),
                button -> toggleAutoHeal(button, activeProfile),
                "autoclicker-fabric.gui.help.auto-heal");

        addCenteredButton(midColX, row3, elementWidth, elementHeight,
                Language.GUI_HUD_ENABLED.getText(AutoClicker.hudConfig.isEnabled()),
                button -> toggleHudEnabled(button),
                "autoclicker-fabric.gui.help.hud-enabled");

        // --- RIGHT COLUMN (Use / Right Click Settings) ---
        addCenteredButton(rightColX, row0, elementWidth, elementHeight,
                Language.GUI_ACTIVE.getText(AutoClicker.rightHolding.isActive()),
                button -> toggleActive(button, AutoClicker.rightHolding),
                "autoclicker-fabric.gui.help.active");

        addCenteredButton(rightColX, row1, elementWidth, elementHeight,
                Language.GUI_SPAMMING.getText(AutoClicker.rightHolding.isSpamming()),
                button -> toggleSpamming(button, AutoClicker.rightHolding),
                "autoclicker-fabric.gui.help.spamming");

        OptionsSliderWidget rightHoldingSpamSpeedSlider = new OptionsSliderWidget(
                rightColX, row2, elementWidth, elementHeight,
                Component.nullToEmpty("Right Spam Speed"),
                AutoClicker.rightHolding.getSpeed() / 1200.0,
                value -> {
                    AutoClicker.rightHolding.setSpeed(value);
                    AutoClicker.getInstance().saveConfig();
                }
        );
        this.addRenderableWidget(rightHoldingSpamSpeedSlider);

        addCenteredButton(rightColX, row3, elementWidth, elementHeight,
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
        super.extractRenderState(context, mouseX, mouseY, delta);

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
