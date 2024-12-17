package net.sxnu.autoclicker;

public class Config {

    private final LeftMouseConfig leftClick;
    private final RightMouseConfig rightClick;
    private final HudConfig hudConfig;

    public Config(LeftMouseConfig leftClick, RightMouseConfig rightClick, HudConfig hudConfig) {
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.hudConfig = hudConfig;
    }

    public LeftMouseConfig getLeftClick() {
        return this.leftClick;
    }

    public RightMouseConfig getRightClick() {
        return this.rightClick;
    }

    public HudConfig getHudConfig(){return this.hudConfig;}

    @Override
    public String toString() {
        return "Config{" +
                "leftClick=" + this.leftClick +
                ", rightClick=" + this.rightClick +
                '}';
    }

    public static class HudConfig {
        private boolean enabled;

        public HudConfig(Boolean enabled){
            this.enabled = enabled;
            }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String toString(){
            return "Config{" +
                    "hudEnabled=" + this.enabled +
                    '}';
        }
    }

    public static class LeftMouseConfig extends SharedConfig {
        private boolean respectCooldown;
        private boolean respectShield;
        private boolean mobMode;

        public LeftMouseConfig(boolean active, boolean spamming, int cpt, boolean respectCooldown, boolean respectShield, boolean mobMode) {
            super(active, spamming, cpt);

            this.respectCooldown = respectCooldown;
            this.mobMode = mobMode;
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

        @Override
        public String toString() {
            return "SharedConfig{" +
                    "active=" + this.active +
                    ", spamming=" + this.spamming +
                    ", cpt=" + this.cpt +
                    '}';
        }
    }
}
