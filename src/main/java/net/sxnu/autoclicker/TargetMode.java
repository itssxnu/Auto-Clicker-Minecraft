package net.sxnu.autoclicker;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;

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
        if (entity instanceof TamableAnimal tameable && tameable.isTame()) {
            return false;
        }

        // Exclude villagers and golems
        if (entity instanceof Villager || entity instanceof AbstractGolem) {
            return false;
        }

        // Exclude other players
        if (entity instanceof Player) {
            return false;
        }

        boolean isHostile = entity instanceof Enemy;
        boolean isPassive = entity instanceof Animal || entity instanceof WaterAnimal || entity instanceof AmbientCreature;

        return switch (this) {
            case HOSTILE -> isHostile;
            case PASSIVE -> isPassive;
            case HOSTILE_AND_PASSIVE -> isHostile || isPassive;
            default -> false;
        };
    }
}
