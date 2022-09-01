package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Random;

import static org.bukkit.entity.EntityType.*;

public class ListenerPlayerDamage implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void playerDamageByEntity(EntityDamageByEntityEvent event) { // change mob damage effects and attack counts etc. if it is hard to do in their custom entity classes
        if (event.getEntityType() == PLAYER) {
            Player bukkitPlayer = (Player)event.getEntity();
            Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();
            World nmsWorld = nmsDamager.getWorld();

            switch (event.getDamager().getType()) {
                case BAT -> ((CustomEntityBat)nmsDamager).attacks++; // increase attack count by 1
                case CAVE_SPIDER -> ((CustomEntitySpiderCave)nmsDamager).attacks++;
                case ENDER_DRAGON -> Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.setVelocity(bukkitPlayer.getVelocity().multiply(10.0)), 2L); /** ender dragon flings players much further */
                case ENDERMAN -> ((CustomEntityEnderman)nmsDamager).attacks++;
                case ENDERMITE -> ((CustomEntityEndermite)nmsDamager).attacks++;
                case HOGLIN -> {
                    CustomEntityHoglin hoglin = (CustomEntityHoglin)nmsDamager;
                    hoglin.attacks++;

                    if (hoglin.attacks >= 10 && hoglin.attacks < 44 && (hoglin.attacks - 15) % 3 == 0) { /** from 10 to 44 attacks, hoglins gain 4 max health every 3 attacks */
                        hoglin.addEffect(new MobEffect(MobEffects.HEALTH_BOOST, Integer.MAX_VALUE, (hoglin.attacks - 15) / 3));
                    } else if (hoglin.attacks >= 44 && hoglin.attacks < 55) { /** from 44 to 55 attacks, hoglin loses 4 max health but gains 1.2 damage per attack */
                        int amp = hoglin.getEffect(MobEffects.HEALTH_BOOST).getAmplifier();
                        hoglin.removeEffect(MobEffects.HEALTH_BOOST);
                        hoglin.addEffect(new MobEffect(MobEffects.HEALTH_BOOST, Integer.MAX_VALUE, amp - 1));

                        hoglin.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(hoglin.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).getValue() + 1.2);
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.setVelocity(new Vector(0.0, 0.8 * (hoglin.isBaby() ? 2.0 : 1.0), 0.0)), 2L); /** hoglins launch players into air, doubled if baby */ // delay by 2 ticks or else the mob's damage knockback is immediately applied after this setvelocity, canceling it out
                }
                case HUSK -> {
                    CustomEntityZombieHusk husk = (CustomEntityZombieHusk)nmsDamager;
                    husk.attacks++;

                    if (husk.attacks < 30) {
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1200, 0)); /** husks apply hunger for 1 minute instead */
                    } else { /** after 30 attacks, husks apply hunger 100 for 5 seconds instead */
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 100, 99));
                    }
                }
                case MAGMA_CUBE -> ((CustomEntitySlimeMagmaCube)nmsDamager).attacks++;
                case IRON_GOLEM -> {
                    CustomEntityIronGolem ironGolem = (CustomEntityIronGolem)nmsDamager;
                    ironGolem.attacks++;
                    ironGolem.increaseStatsAdd(2.0, 0.5, 0.025);

                    if (ironGolem.attacks == 10 || ironGolem.attacks == 20 || ironGolem.attacks == 25 || ironGolem.attacks == 30 || ironGolem.attacks == 35 || ironGolem.attacks == 40 || ironGolem.attacks == 43 || ironGolem.attacks == 46 || ironGolem.attacks == 49 || ironGolem.attacks >= 50) { /** on these attacks, iron golems knock players high into the air */
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.setVelocity(new Vector(0.0, 2.0, 0.0)), 2L);
                    }
                }
                case PHANTOM -> ((CustomEntityPhantom)nmsDamager).attacks++;
                case PIGLIN -> {
                    CustomEntityPiglin piglin = (CustomEntityPiglin)nmsDamager;
                    piglin.attacks++;
                    piglin.setHealth((float)(piglin.getHealth() + 0.75)); /** piglins heal by 0.75 every time its attacks increase by 1 */
                }
                case RABBIT -> ((CustomEntityRabbit)nmsDamager).attacks++;
                case RAVAGER -> {
                    CustomEntityRavager ravager = (CustomEntityRavager)nmsDamager;
                    ravager.attacks++;

                    if (ravager.launchHigh) {
                        ravager.launchHigh = false;
                        event.setDamage(6.0);
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.setVelocity(new Vector(0.0, 2.0, 0.0)), 2L);
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                        if (Math.abs(bukkitPlayer.getVelocity().getX()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getY()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getZ()) < 0.14) { /** if the player has not moved much after 5 ticks (meaning it did not get knockbacked enough), the next attack the player will be flung high into the air if they are jumping and damage will be increased to 6 */
                            ravager.launchHigh = true;
                        }
                    }, 5L);
                }
                case SHEEP -> {
                    if (!(nmsDamager instanceof CustomEntitySheepAggressive)) {
                        return;
                    }

                    CustomEntitySheepAggressive sheep = (CustomEntitySheepAggressive)nmsDamager;
                    sheep.attacks++;

                    if (sheep.launchHigh) {
                        sheep.launchHigh = false;
                        event.setDamage(9.0);
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.setVelocity(new Vector(0.0, 1.5, 0.0)), 2L);
                    }

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> sheep.launchHigh = Math.abs(bukkitPlayer.getVelocity().getX()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getY()) < 0.14 && Math.abs(bukkitPlayer.getVelocity().getZ()) < 0.14, 5L); /** if the player has not moved much after 5 ticks (meaning it did not get knockbacked enough), the next attack the player will be flung high into the air if they are jumping and damage will be increased to 9 */
                }
                case SILVERFISH -> {
                    CustomEntitySilverfish silverfish = (CustomEntitySilverfish)nmsDamager;
                    silverfish.attacks++;

                    if (silverfish.attacks > 60 && random.nextDouble() < 0.2) { /** silverfish hava a 20% chance to duplicate when hitting a player after 60 attacks */
                        new SpawnEntity(nmsWorld, new CustomEntitySilverfish(nmsWorld), 1, null, null, silverfish, false, true);
                    }
                }
                case SLIME -> ((CustomEntitySlime)nmsDamager).attacks++;
                case SPIDER -> {
                    CustomEntitySpider spider = (CustomEntitySpider)nmsDamager;
                    spider.attacks++;
                    bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 0)); /** spiders inflict slowness 1 for 1.5 secondS on hit */

                    if ((spider).attacks >= 25) {
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 25, 0)); /** spiders inflict poison 1 for 2 damage ticks on hit if it has attacked more than 25 times */
                    }
                }
                case VEX -> ((CustomEntityVex)nmsDamager).attacks++;
                case VINDICATOR -> ((CustomEntityVindicator)nmsDamager).attacks++;
                case WITHER_SKELETON -> ((CustomEntitySkeletonWither)nmsDamager).attacks++;
                case ZOGLIN -> {
                    CustomEntityZoglin zoglin = (CustomEntityZoglin)nmsDamager;
                    zoglin.attacks++;

                    if (zoglin.attacks >= 40) {
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> bukkitPlayer.setVelocity(new Vector(0.0, 0.75, 0.0)), 2L); /** after 40 attacks, zoglins throw players into the air when it hits the player */
                    }
                }
                case ZOMBIE -> {
                    org.bukkit.World bukkitWorld = bukkitPlayer.getWorld();

                    if (nmsDamager instanceof CustomEntityZombieThor) {
                        bukkitWorld.strikeLightning(bukkitPlayer.getLocation());
                    } else {
                        CustomEntityZombie zombie = (CustomEntityZombie)nmsDamager;
                        zombie.attacks++;

                        if (zombie.attacks >= 30) { /** after 30 attacks, zombies summon vanilla lightning on the player when it hits the player */
                            bukkitWorld.strikeLightning(bukkitPlayer.getLocation());
                        }
                    }
                }
                case ZOMBIE_VILLAGER -> ((CustomEntityZombieVillager)nmsDamager).attacks++;
                case ZOMBIFIED_PIGLIN -> ((CustomEntityZombiePig)nmsDamager).attacks++;
            }

            if (bukkitPlayer.isBlocking()) { /** all mob attacks damage shields 75% more (at least 4 damage) */
                event.setDamage(Math.max(event.getDamage() * 1.75, 4.0));
            }
        }
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            EntityPlayer nmsPlayer = (EntityPlayer)((CraftEntity) event.getEntity()).getHandle();
            double damage = event.getDamage();

            if (cause.equals(EntityDamageEvent.DamageCause.DROWNING)) { /** drowning damage has a 50% chance to spawn a pufferfish, and a 15% chance to spawn a guardian */
                World nmsWorld = nmsPlayer.getWorld();

                if (random.nextDouble() < 0.5) {
                    new SpawnEntity(nmsWorld, new CustomEntityPufferfish(nmsWorld), 1, null, null, nmsPlayer, false, true);
                } else if (random.nextDouble() < 0.15) {
                    new SpawnEntity(nmsWorld, new CustomEntityGuardian(nmsWorld), 1, null, null, nmsPlayer, false, true);
                }
            }

            switch (cause) {
                case CONTACT -> event.setDamage(10.0); /** cactus do 10 damage instead of 1 */
                case FALLING_BLOCK -> event.setDamage(damage * 0.5); /** anvils do 50% less damage */
                case FIRE -> { /** soul fire sets players on fire for 10 minutes */
                    if (event.getEntity().getWorld().getBlockAt((int)nmsPlayer.locX(), (int)nmsPlayer.locY(), (int)nmsPlayer.locZ()).getType() == org.bukkit.Material.SOUL_FIRE) {
                        nmsPlayer.setFireTicks(12000);
                    }
                }
                case MAGIC -> event.setDamage(damage * 0.333333333); /** harming potions and area effect clouds do 67% less damage */
                case SUFFOCATION -> event.setDamage(5.0); /** suffocation does 5 damage instead of 1 */
            }
        }
    }

    @EventHandler
    public void entityAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == PLAYER && event.getAmount() >= 2) { /** player air goes down 2 times as fast */
            event.setAmount(event.getAmount() - 1);
        }
    }
}
