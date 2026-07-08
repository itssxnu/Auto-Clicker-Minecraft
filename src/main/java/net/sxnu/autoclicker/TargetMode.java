package net.sxnu.autoclicker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;

public enum TargetMode {
    ALL("all"),
    HOSTILE("hostile"),
    PASSIVE("passive"),
    HOSTILE_AND_PASSIVE("hostile_and_passive");

    private final String translationKey;

    TargetMode(String key) {
        this.translationKey = "autoclicker-fabric.gui.target." + key;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public boolean isValidTarget(LivingEntity entity) {
        if (this == ALL) {
            return true;
        }

        // Tameable animals that are tamed should be excluded
        if (entity instanceof TameableEntity tameable && tameable.isTamed()) {
            return false;
        }

        // Exclude villagers and golems
        if (entity instanceof VillagerEntity || entity instanceof GolemEntity) {
            return false;
        }

        // Exclude other players
        if (entity instanceof PlayerEntity) {
            return false;
        }

        boolean isHostile = entity instanceof Monster;
        boolean isPassive = entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity || entity instanceof AmbientEntity;

        return switch (this) {
            case HOSTILE -> isHostile;
            case PASSIVE -> isPassive;
            case HOSTILE_AND_PASSIVE -> isHostile || isPassive;
            default -> false;
        };
    }
}
