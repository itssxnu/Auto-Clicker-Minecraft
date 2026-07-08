package net.sxnu.autoclicker;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class AutoClicker {
    public static final String MOD_ID = "autoclicker";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final KeyMapping openConfig =
            new KeyMapping("keybinding.open-gui", GLFW.GLFW_KEY_O, KeyMapping.Category.MISC);
    public static final KeyMapping toggleHolding =
            new KeyMapping("keybinding.toggle-hold", GLFW.GLFW_KEY_I, KeyMapping.Category.MISC);
    private static final Path CONFIG_DIR = Paths.get(Minecraft.getInstance().gameDirectory.getPath() + "/config");
    private static final Path CONFIG_FILE = Paths.get(CONFIG_DIR + "/auto-clicker-fabric.json");
    public static Holding.AttackHolding leftHolding;
    public static Holding rightHolding;
    public static Config.HudConfig hudConfig;
    private static AutoClicker INSTANCE;
    private boolean isActive = false;

    // Auto-bridging states
    private static boolean isAutoSneaking = false;
    private static boolean isAutoRightClicking = false;
    private static net.minecraft.world.item.Item lastBridgingBlock = null;

    // Auto-eat/heal states
    private static int originalSlot = -1;
    private static int eatingTicks = 0;
    private static boolean isEatingOrDrinking = false;

    private static Config createDefaultConfig() {
        java.util.Map<String, Config.ProfileConfig> profiles = new java.util.HashMap<>();
        
        // Default profile
        profiles.put("Default", new Config.ProfileConfig(
            new Config.LeftMouseConfig(false, false, 0, false, false, false, TargetMode.ALL),
            new Config.RightMouseConfig(false, false, 0),
            false, false, false
        ));
        
        // PVP profile
        profiles.put("PVP Mode", new Config.ProfileConfig(
            new Config.LeftMouseConfig(false, false, 0, true, true, false, TargetMode.ALL),
            new Config.RightMouseConfig(false, false, 0),
            false, false, false
        ));
        
        // AFK Mob Farm profile
        profiles.put("AFK Mob Farm", new Config.ProfileConfig(
            new Config.LeftMouseConfig(true, false, 0, true, false, true, TargetMode.HOSTILE),
            new Config.RightMouseConfig(false, false, 0),
            false, false, false
        ));

        // Block Bridging profile
        profiles.put("Block Bridging", new Config.ProfileConfig(
            new Config.LeftMouseConfig(false, false, 0, false, false, false, TargetMode.ALL),
            new Config.RightMouseConfig(true, true, 1),
            true, false, false
        ));

        return new Config(profiles, new Config.HudConfig(true), "Default");
    }

    private Config config = createDefaultConfig();

    public AutoClicker() {
        INSTANCE = this;
    }

    public static AutoClicker getInstance() {
        return INSTANCE;
    }

    public void onInitialize() {
        LOGGER.info("Auto Clicker Initialised");
    }

    public void clientReady(Minecraft client) {
        if (!Files.exists(CONFIG_FILE)) {
            try {
                Files.createDirectories(CONFIG_DIR);
                Files.createFile(CONFIG_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.saveConfig();
        } else {
            try {
                FileReader json = new FileReader(CONFIG_FILE.toFile());
                Config config = new Gson().fromJson(json, Config.class);
                json.close();
                if (config != null && config.getHudConfig() != null) {
                    this.config = config;
                    // Migrate legacy config if profiles is missing
                    if (this.config.getProfiles() == null || this.config.getProfiles().isEmpty()) {
                        Config oldConfig = this.config;
                        this.config = createDefaultConfig();
                        Config.ProfileConfig defaultProf = this.config.getProfiles().get("Default");
                        if (defaultProf != null) {
                            if (oldConfig.leftClick != null) {
                                defaultProf.setLeftClick(oldConfig.leftClick);
                            }
                            if (oldConfig.rightClick != null) {
                                defaultProf.setRightClick(oldConfig.rightClick);
                            }
                        }
                        this.saveConfig();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                this.saveConfig();
            }
        }

        leftHolding = new Holding.AttackHolding(client.options.keyAttack, this.config.getLeftClick());
        rightHolding = new Holding(client.options.keyUse, this.config.getRightClick());
        hudConfig = this.config.getHudConfig();
    }

    public void saveConfig() {
        try {
            FileWriter writer = new FileWriter(CONFIG_FILE.toFile());

            new Gson().toJson(this.config, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RenderGameOverlayEvent(GuiGraphicsExtractor context, DeltaTracker delta) {
        if (!this.isActive || !config.getHudConfig().isEnabled()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        int color = 0xFFFFFF; // White color

        // Collect lines to render dynamically
        java.util.List<Component> lines = new java.util.ArrayList<>();
        lines.add(Component.nullToEmpty("Auto Clicker: Active (" + config.getActiveProfile() + ")"));

        if (leftHolding.isActive()) {
            String mode = leftHolding.isSpamming() ? "Spam" : (leftHolding.isRespectCooldown() ? "Cooldown" : "Hold");
            lines.add(Component.nullToEmpty("• Left Click: " + mode));
        }
        if (rightHolding.isActive()) {
            String mode = rightHolding.isSpamming() ? "Spam" : "Hold";
            lines.add(Component.nullToEmpty("• Right Click: " + mode));
        }
        Config.ProfileConfig activeProfile = config.getActiveProfileConfig();
        if (activeProfile != null) {
            if (activeProfile.isAutoBridge()) {
                lines.add(Component.nullToEmpty("• Auto-Bridge: Active"));
            }
            if (activeProfile.isAutoEat()) {
                lines.add(Component.nullToEmpty("• Auto-Eat: Active"));
            }
            if (activeProfile.isAutoHeal()) {
                lines.add(Component.nullToEmpty("• Auto-Heal: Active"));
            }
        }

        int startY = screenHeight - 70;
        int spacing = 12;

        for (int i = 0; i < lines.size(); i++) {
            Component text = lines.get(i);
            int textWidth = client.font.width(text);
            int x = (screenWidth - textWidth) / 2;
            int y = startY + (i * spacing);
            context.text(client.font, text.getVisualOrderText(), x, y, color, true);
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public void clientTickEvent(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!mc.player.isAlive()) {
            this.isActive = false;
            if (isAutoSneaking) {
                mc.options.keyShift.setDown(false);
                isAutoSneaking = false;
            }
            if (isAutoRightClicking) {
                mc.options.keyUse.setDown(false);
                isAutoRightClicking = false;
            }
            isEatingOrDrinking = false;
            originalSlot = -1;
            lastBridgingBlock = null;
        }

        // Get the active profile config
        Config.ProfileConfig activeProfile = config.getActiveProfileConfig();

        if (this.isActive && activeProfile != null) {
            // Auto-Heal check (highest priority)
            if (activeProfile.isAutoHeal() && !isEatingOrDrinking && mc.player.getHealth() <= 10.0f) {
                int healSlot = findHealSlot(mc.player);
                if (healSlot != -1) {
                    originalSlot = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(healSlot);
                    mc.options.keyUse.setDown(true);
                    isEatingOrDrinking = true;
                    eatingTicks = 0;
                }
            }

            // Auto-Eat check (lower priority)
            if (activeProfile.isAutoEat() && !isEatingOrDrinking && mc.player.getFoodData().getFoodLevel() <= 12) {
                int foodSlot = findFoodSlot(mc.player);
                if (foodSlot != -1) {
                    originalSlot = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(foodSlot);
                    mc.options.keyUse.setDown(true);
                    isEatingOrDrinking = true;
                    eatingTicks = 0;
                }
            }

            // If currently eating or drinking
            if (isEatingOrDrinking) {
                eatingTicks++;
                // Check if current item finished consuming (isUsingItem goes false) or safety timeout
                if (eatingTicks > 5 && (!mc.player.isUsingItem() || eatingTicks > 45)) {
                    // Check if the item in hand is a healing item
                    boolean isHealItem = false;
                    ItemStack activeStack = mc.player.getInventory().getItem(mc.player.getInventory().getSelectedSlot());
                    if (!activeStack.isEmpty()) {
                        if (activeStack.is(Items.GOLDEN_APPLE) || activeStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                            isHealItem = true;
                        } else if (activeStack.getItem() instanceof net.minecraft.world.item.PotionItem) {
                            isHealItem = true;
                        }
                    }

                    // If it was food and hunger is still not full, continue eating
                    if (!isHealItem && mc.player.getFoodData().getFoodLevel() < 20) {
                        int nextFoodSlot = findFoodSlot(mc.player);
                        if (nextFoodSlot != -1) {
                            mc.player.getInventory().setSelectedSlot(nextFoodSlot);
                            mc.options.keyUse.setDown(true);
                            eatingTicks = 0; // Reset ticks to wait for next item consumption
                        } else {
                            // Out of food, stop
                            mc.options.keyUse.setDown(false);
                            if (originalSlot != -1) {
                                mc.player.getInventory().setSelectedSlot(originalSlot);
                            }
                            isEatingOrDrinking = false;
                            originalSlot = -1;
                            eatingTicks = 0;
                        }
                    } else {
                        // Healing item finished or hunger is full, stop eating
                        mc.options.keyUse.setDown(false);
                        if (originalSlot != -1) {
                            mc.player.getInventory().setSelectedSlot(originalSlot);
                        }
                        isEatingOrDrinking = false;
                        originalSlot = -1;
                        eatingTicks = 0;
                    }
                }
            }

            // Auto-Bridge (SafeWalk/Eagle) checking
            boolean holdsBlockInMain = mc.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BlockItem;
            boolean holdsBlockInOff = mc.player.getOffhandItem().getItem() instanceof net.minecraft.world.item.BlockItem;

            // Record last block type used
            if (holdsBlockInMain) {
                lastBridgingBlock = mc.player.getMainHandItem().getItem();
            } else if (holdsBlockInOff) {
                lastBridgingBlock = mc.player.getOffhandItem().getItem();
            }

            boolean isMovingBackwards = mc.options.keyDown.isDown() || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();

            // Only switch slots if the user is actively attempting to bridge (moving backwards/sideways on ground)
            boolean isAttemptingToBridge = activeProfile.isAutoBridge()
                && !isEatingOrDrinking
                && mc.player.onGround()
                && isMovingBackwards;

            if (isAttemptingToBridge && mc.player.getMainHandItem().isEmpty() && lastBridgingBlock != null) {
                int slot = findSameBlockSlot(mc.player, lastBridgingBlock);
                if (slot != -1) {
                    mc.player.getInventory().setSelectedSlot(slot);
                    holdsBlockInMain = true; // Switch successful!
                }
            }

            boolean hasBlocks = holdsBlockInMain || holdsBlockInOff;
            boolean isBridging = isAttemptingToBridge && hasBlocks;

            // Block depletion safety:
            // If they run out of blocks but are still trying to move backwards/sideways,
            // we force-sneak at the edge to prevent them from falling off!
            boolean forceSafetySneak = activeProfile.isAutoBridge()
                && !isEatingOrDrinking
                && !hasBlocks
                && isMovingBackwards;

            if (isBridging || forceSafetySneak) {
                // Shrink the bounding box to check if the player's center position is off the edge (contract by 0.275 on each side)
                boolean onEdge = !mc.level.getBlockCollisions(mc.player, mc.player.getBoundingBox().move(0, -0.1, 0).deflate(0.275, 0, 0.275)).iterator().hasNext();
                if (onEdge) {
                    mc.options.keyShift.setDown(true);
                    isAutoSneaking = true;

                    // Trigger block placement (right-click) if they have blocks
                    if (hasBlocks) {
                        mc.options.keyUse.setDown(true);
                        isAutoRightClicking = true;
                    } else {
                        if (isAutoRightClicking) {
                            mc.options.keyUse.setDown(false);
                            isAutoRightClicking = false;
                        }
                    }
                } else {
                    if (isAutoSneaking) {
                        mc.options.keyShift.setDown(false);
                        isAutoSneaking = false;
                    }
                    if (isAutoRightClicking) {
                        mc.options.keyUse.setDown(false);
                        isAutoRightClicking = false;
                    }
                }
            } else {
                if (isAutoSneaking) {
                    mc.options.keyShift.setDown(false);
                    isAutoSneaking = false;
                }
                if (isAutoRightClicking) {
                    mc.options.keyUse.setDown(false);
                    isAutoRightClicking = false;
                }
            }

            // Only run left click/right click handles if not actively auto-eating/drinking and not auto-bridging placement
            if (!isEatingOrDrinking) {
                if (leftHolding.isActive()) {
                    this.handleActiveHolding(mc, leftHolding);
                }

                if (rightHolding.isActive() && !isAutoRightClicking) {
                    this.handleActiveHolding(mc, rightHolding);
                }
            }
        } else {
            // Reset auto states when not active
            if (isAutoSneaking) {
                mc.options.keyShift.setDown(false);
                isAutoSneaking = false;
            }
            if (isAutoRightClicking) {
                mc.options.keyUse.setDown(false);
                isAutoRightClicking = false;
            }
            if (isEatingOrDrinking) {
                mc.options.keyUse.setDown(false);
                if (originalSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(originalSlot);
                }
                isEatingOrDrinking = false;
                originalSlot = -1;
            }
            lastBridgingBlock = null;
        }

        this.keyInputEvent(mc);
    }

    private int findSameBlockSlot(LocalPlayer player, net.minecraft.world.item.Item blockItem) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == blockItem) {
                return i;
            }
        }
        return -1;
    }

    private int findFoodSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                // Exclude precious foods (Golden Apples / Enchanted Golden Apples) from normal eating
                if (!stack.is(Items.GOLDEN_APPLE) && !stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int findHealSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
                    return i;
                }
                if (stack.getItem() instanceof net.minecraft.world.item.PotionItem) {
                    PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
                    if (potionContents != null && potionContents.potion().isPresent()) {
                        String path = potionContents.potion().get().unwrapKey().map(key -> key.identifier().getPath()).orElse("");
                        if (path.contains("healing") || path.contains("regeneration")) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private void handleActiveHolding(Minecraft mc, Holding key) {
        assert mc.player != null;
        if (!key.isActive()) {
            return;
        }

        if (key.isSpamming()) {
            // How to handle the click if it's done by spamming
            if (key.getSpeed() > 0) {
                if (key.getTimeout() <= 1) {
                    if (key.getTimeout() <= 0) {
                        key.resetTimeout();
                    }

                    // Press the button twice by toggling 1 and 0
                    key.getKey().setDown(key.getTimeout() == 1);

                    if (key.getKey().isDown()) {
                        this.attemptMobAttack(mc, key);
                    }
                }
                key.decreaseTimeout();
            } else {
                // Handle the click if it's done normally
                key.getKey().setDown(!key.getKey().isDown());
                if (key.getKey().isDown()) {
                    this.attemptMobAttack(mc, key);
                }
            }

            return;
        }

        // Normal holding or cool down behaviour
        // respect cool down
        if (key.isRespectCooldown()) {
            // Don't do anything if they're not looking at something
            if (key instanceof Holding.AttackHolding && ((Holding.AttackHolding) key).isMobMode() && !this.isPlayerLookingAtMob(mc)) {
                if (key.getKey().isDown()) {
                    key.getKey().setDown(false);
                }
                return;
            }

            if (mc.player.getAttackStrengthScale(0) == 1.0F) {
                key.getKey().setDown(true);
                this.attemptMobAttack(mc, key);
            } else {
                key.getKey().setDown(false);
            }
        } else {
            // Hold the click
            key.getKey().setDown(true);
        }
    }

    private void attemptMobAttack(Minecraft mc, Holding key) {
        // Don't attack on a right click
        if (key.getKey() != leftHolding.getKey()) {
            return;
        }

        HitResult rayTrace = mc.hitResult;
        if (rayTrace instanceof EntityHitResult entityHit && mc.gameMode != null) {
            if (!(config.getLeftClick().isRespectShield() && isShielding(mc.player))) {
                if (entityHit.getEntity() instanceof LivingEntity livingEntity) {
                    if (!leftHolding.getTargetMode().isValidTarget(livingEntity)) {
                        return;
                    }
                } else if (leftHolding.isMobMode()) {
                    return;
                }
                mc.gameMode.attack(mc.player, entityHit.getEntity());
                if (mc.player != null) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    private boolean isShielding(LocalPlayer player) {
        if (player.isUsingItem()) {
            return player.getUseItem().getItem() instanceof ShieldItem;
        }
        return false;
    }

    private boolean isPlayerLookingAtMob(Minecraft mc) {
        HitResult rayTrace = mc.hitResult;
        return rayTrace instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingEntity && livingEntity.isAlive() && livingEntity.isAttackable() && leftHolding.getTargetMode().isValidTarget(livingEntity);
    }

    private void keyInputEvent(Minecraft mc) {
        assert mc.player != null;
        while (toggleHolding.consumeClick()) {
            this.isActive = !this.isActive;
            mc.player.sendOverlayMessage(
                    (this.isActive ? Language.MSG_HOLDING_KEYS : Language.MSG_RELEASED_KEYS)
                            .getText()
                            .withStyle(this.isActive ? ChatFormatting.GREEN : ChatFormatting.RED)
            );

            if (!this.isActive) {
                if(leftHolding.isActive()) leftHolding.getKey().setDown(false);
                if(rightHolding.isActive()) rightHolding.getKey().setDown(false);
            }
        }

        while (openConfig.consumeClick()) {
            mc.setScreenAndShow(getConfigScreen());
        }
    }

    public OptionsScreen getConfigScreen(){
        return new OptionsScreen();
    }
}