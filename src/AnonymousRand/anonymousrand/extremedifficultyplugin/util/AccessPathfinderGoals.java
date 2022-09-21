package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Set;

public class AccessPathfinderGoals {

    private static Field goalSet;

    static {
        try {
            goalSet = PathfinderGoalSelector.class.getDeclaredField("d"); // get list of goals from original entity (not just running/active goals)
            goalSet.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<PathfinderGoal> getPathfinderGoals(Set<?> goalSet, Class<? extends PathfinderGoal> pathfinderGoalClass) {
        ArrayList<PathfinderGoal> goalsFound = new ArrayList<>();
        PathfinderGoal pathfinderGoal;

        for (Object pathfinderGoalWrapped : goalSet) {
            try {
                Field pathfinderGoalField = pathfinderGoalWrapped.getClass().getDeclaredField("a"); // a is the field that contains the pathfinder goal in the wrapped pathfinder goal object
                pathfinderGoalField.setAccessible(true);
                pathfinderGoal = (PathfinderGoal)pathfinderGoalField.get(pathfinderGoalWrapped);

                if (pathfinderGoalClass.isInstance(pathfinderGoal)) {
                    goalsFound.add(pathfinderGoal);
                }
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                e.printStackTrace();
            }
        }

        return goalsFound;
    }

    public static void removePathfinderGoals(EntityInsentient entity) {
        ArrayList<PathfinderGoal> goalsToRemove = new ArrayList<>();

        try {
            switch (entity.getBukkitEntity().getType()) { // need to do this instead of just taking the goals out of the custom entity's target selector because for some reason the custom entity's target selector's Field d doesn't have the super (vanilla) ones
                case BEE -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityBee)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityBee)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case BLAZE -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityBlaze)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityBlaze)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case ENDERMITE -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityEndermite)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityEndermite)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case HOGLIN -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityHoglin)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityHoglin)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case HUSK -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityZombieHusk)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZombieHusk)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case MAGMA_CUBE -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntitySlimeMagmaCube)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntitySlimeMagmaCube)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case PILLAGER -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityPillager)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityPillager)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case RABBIT -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityRabbit)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityRabbit)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case RAVAGER -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityRavager)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityRavager)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case SILVERFISH -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntitySilverfish)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntitySilverfish)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case SLIME -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntitySlime)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntitySlime)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case VEX -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityVex)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityVex)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case VINDICATOR -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityVindicator)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityVindicator)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case WITCH -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityWitch)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityWitch)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case WITHER_SKELETON -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntitySkeletonWither)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntitySkeletonWither)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case ZOGLIN -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityZoglin)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZoglin)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case ZOMBIE -> {
                    if (entity instanceof CustomEntityZombieThor) {
                        goalsToRemove = getPathfinderGoals((Set) goalSet.get(((CustomEntityZombieThor) entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                        goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZombieThor)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                    } else if (entity instanceof CustomEntityZombieSuper) {
                        goalsToRemove = getPathfinderGoals((Set) goalSet.get(((CustomEntityZombieSuper) entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                        goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZombieSuper)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                    } else {
                        goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityZombie)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                        goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZombie)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                    }
                }
                case ZOMBIE_VILLAGER -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityZombieVillager)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZombieVillager)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
                case ZOMBIFIED_PIGLIN -> {
                    goalsToRemove = getPathfinderGoals((Set)goalSet.get(((CustomEntityZombiePig)entity).targetSelectorVanilla), PathfinderGoalHurtByTarget.class);
                    goalsToRemove.addAll(getPathfinderGoals((Set)goalSet.get(((CustomEntityZombiePig)entity).targetSelectorVanilla), PathfinderGoalNearestAttackableTarget.class));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (goalsToRemove.size() > 0) {
            for (PathfinderGoal goal : goalsToRemove) { // but somehow removing vanilla goals from custom target selectors still works
                if (!(goal instanceof CustomPathfinderGoalHurtByTarget) && !(goal instanceof CustomPathfinderGoalNearestAttackableTarget)) {
                    entity.targetSelector.a(goal); // remove goal
                }
            }

            if (entity instanceof EntityCreature) {
                entity.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget((EntityCreature)entity, new Class[0])); /** custom goal that prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
            }
        }
    }
}