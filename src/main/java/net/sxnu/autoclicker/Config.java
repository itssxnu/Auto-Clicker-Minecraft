package net.sxnu.autoclicker;

import java.util.Map;

public class Config {

    // Backwards compatibility fields for older config versions
    LeftMouseConfig leftClick;
    RightMouseConfig rightClick;

    private String activeProfile;
    private final Map<String, ProfileConfig> profiles;
    private final HudConfig hudConfig;

    public Config(Map<String, ProfileConfig> profiles, HudConfig hudConfig, String activeProfile) {
        this.profiles = profiles;
        this.hudConfig = hudConfig;
        this.activeProfile = activeProfile;
    }

    public String getActiveProfile() {
        return this.activeProfile == null ? "Default" : this.activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public Map<String, ProfileConfig> getProfiles() {
        return this.profiles;
    }

    public ProfileConfig getActiveProfileConfig() {
        String active = getActiveProfile();
        if (this.profiles == null) {
            return null;
        }
        if (!this.profiles.containsKey(active)) {
            if (this.profiles.containsKey("Default")) {
                return this.profiles.get("Default");
            }
            // Create Default profile if missing
            ProfileConfig defaultProfile = new ProfileConfig(
                new LeftMouseConfig(false, false, 0, false, false, false, TargetMode.ALL),
                new RightMouseConfig(false, false, 0),
                false, false, false
            );
            this.profiles.put("Default", defaultProfile);
            return defaultProfile;
        }
        return this.profiles.get(active);
    }

    public LeftMouseConfig getLeftClick() {
        ProfileConfig active = getActiveProfileConfig();
        return active != null ? active.getLeftClick() : new LeftMouseConfig(false, false, 0, false, false, false, TargetMode.ALL);
    }

    public RightMouseConfig getRightClick() {
        ProfileConfig active = getActiveProfileConfig();
        return active != null ? active.getRightClick() : new RightMouseConfig(false, false, 0);
    }

    public HudConfig getHudConfig() {
        return this.hudConfig;
    }

    public static class HudConfig {
        private boolean enabled;

        public HudConfig(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ProfileConfig {
        private LeftMouseConfig leftClick;
        private RightMouseConfig rightClick;
        private boolean autoBridge;
        private boolean autoEat;
        private boolean autoHeal;

        public ProfileConfig(LeftMouseConfig leftClick, RightMouseConfig rightClick, boolean autoBridge, boolean autoEat, boolean autoHeal) {
            this.leftClick = leftClick;
            this.rightClick = rightClick;
            this.autoBridge = autoBridge;
            this.autoEat = autoEat;
            this.autoHeal = autoHeal;
        }

        public LeftMouseConfig getLeftClick() {
            return this.leftClick;
        }

        public void setLeftClick(LeftMouseConfig leftClick) {
            this.leftClick = leftClick;
        }

        public RightMouseConfig getRightClick() {
            return this.rightClick;
        }

        public void setRightClick(RightMouseConfig rightClick) {
            this.rightClick = rightClick;
        }

        public boolean isAutoBridge() {
            return this.autoBridge;
        }

        public void setAutoBridge(boolean autoBridge) {
            this.autoBridge = autoBridge;
        }

        public boolean isAutoEat() {
            return this.autoEat;
        }

        public void setAutoEat(boolean autoEat) {
            this.autoEat = autoEat;
        }

        public boolean isAutoHeal() {
            return this.autoHeal;
        }

        public void setAutoHeal(boolean autoHeal) {
            this.autoHeal = autoHeal;
        }
    }

    public static class LeftMouseConfig extends SharedConfig {
        private boolean respectCooldown;
        private boolean respectShield;
        private boolean mobMode;
        private TargetMode targetMode;

        public LeftMouseConfig(boolean active, boolean spamming, int cpt, boolean respectCooldown, boolean respectShield, boolean mobMode, TargetMode targetMode) {
            super(active, spamming, cpt);

            this.respectCooldown = respectCooldown;
            this.respectShield = respectShield;
            this.mobMode = mobMode;
            this.targetMode = targetMode;
        }

        public boolean isRespectCooldown() {
            return this.respectCooldown;
        }

        public void setRespectCooldown(boolean respectCooldown) {
            this.respectCooldown = respectCooldown;
        }

        public boolean isRespectShield() {
            return this.respectShield;
        }

        public void setRespectShield(boolean respectShield) {
            this.respectShield = respectShield;
        }

        public boolean isMobMode() {
            return this.mobMode;
        }

        public void setMobMode(boolean mobMode) {
            this.mobMode = mobMode;
        }

        public TargetMode getTargetMode() {
            return this.targetMode == null ? TargetMode.ALL : this.targetMode;
        }

        public void setTargetMode(TargetMode targetMode) {
            this.targetMode = targetMode;
        }
    }

    public static class RightMouseConfig extends SharedConfig {
        public RightMouseConfig(boolean active, boolean spamming, int cpt) {
            super(active, spamming, cpt);
        }
    }

    public static class SharedConfig {
        private boolean active;
        private boolean spamming;
        private int cpt;

        public SharedConfig(boolean active, boolean spamming, int cpt) {
            this.active = active;
            this.spamming = spamming;
            this.cpt = cpt;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isSpamming() {
            return this.spamming;
        }

        public void setSpamming(boolean spamming) {
            this.spamming = spamming;
        }

        public int getCpt() {
            return this.cpt;
        }

        public void setCpt(int cpt) {
            this.cpt = cpt;
        }
    }
}
