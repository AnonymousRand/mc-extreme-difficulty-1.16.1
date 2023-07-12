package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerLightningStrike;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableLightningStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.UUID;

public class CustomEntityZombieVillager extends EntityZombieVillager implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a25, deathLightningStorm;

    public CustomEntityZombieVillager(World world) {
        super(EntityTypes.ZOMBIE_VILLAGER, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a25 = false;
        this.deathLightningStorm = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.69); /** zombie villgers move 3x faster */
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /** no longer targets iron golems */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlockLookingAt(this)); /** custom goal that allows the mob to break the block it is looking at every 4 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300.0, 0.0015)); /** custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class)); /** prioritizes villagers over players */
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, EntityTurtle.bv));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void startConversion(@Nullable UUID uuid, int i) { /** zombie villagers can't be converted and instead summon 10 more zombie villagers when fed a golden apple */
        new SpawnEntity(this.getWorld(), new CustomEntityZombieVillager(this.getWorld()), 10, null, null, this, false, true);
        this.world.broadcastEntityEffect(this, (byte) 16);
    }

    public double getFollowRange() { /** zombies have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 25 && !this.a25) { /** after 25 attacks, zombie villagers get regen 4 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 3));
        }

        if (this.getHealth() <= 0.0 && this.attacks >= 40 && !this.deathLightningStorm) { // do this here instead of in die() so that the storm doesn't have to wait until the death animation finishes playing to start
            this.deathLightningStorm = true;
            ListenerLightningStrike.storm = true;
            new RunnableLightningStorm(this.getWorld(), new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ()), random.nextInt(10) + 50).runTaskTimer(StaticPlugin.plugin, 0L, random.nextInt(3) + 3); /** after 40 attacks, zombie villagers summon a lightning storm when killed */
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}
