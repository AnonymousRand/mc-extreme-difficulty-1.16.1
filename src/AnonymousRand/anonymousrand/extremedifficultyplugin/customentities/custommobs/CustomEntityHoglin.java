package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class CustomEntityHoglin extends EntityHoglin implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    private AttackLevelingController attackLevelingController;
    public PathfinderGoalSelector vanillaTargetSelector;

    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.initCustom();
        this.initAttackLevelingMob();
        this.initGoalRemovingMob();
    }

    private void initCustom() {
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.initAttributes();
    }

    private void initAttributes() {
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7); /* hoglins move 75% faster (125% faster for babies), do 5 damage (8 for babies), and have extra knockback */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 8.0 : 5.0);
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getFollowRange() { /* hoglins have 40 block detection range (setting attribute doesn't work) (64 after 10 attacks) */
        return (this.attackLevelingController == null || this.getAttacks() < 10) ? 40.0 : 64.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
                double distSquaredToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSquared = forceDespawnDist * forceDespawnDist;

                if (distSquaredToNearestPlayer > (double) forceDespawnDistSquared
                        && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSquared = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSquaredToNearestPlayer
                        > (double) randomDespawnDistSquared && this.isTypeNotPersistent(distSquaredToNearestPlayer)) {
                    this.die();
                } else if (distSquaredToNearestPlayer < (double) randomDespawnDistSquared) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double distX = this.locX() - x;
        double distZ = this.locZ() - z;

        return distX * distX + distZ * distZ;
    }

    @Override
    public double d(Vec3D vec3d) {
        double distX = this.locX() - vec3d.x;
        double distZ = this.locZ() - vec3d.z;

        return distX * distX + distZ * distZ;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                    IAttackLevelingMob                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(10, 20, 40, 75);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        for (int metThreshold : this.attackLevelingController.increaseAttacks(increase)) {
            int[] attackThresholds = this.attackLevelingController.getAttacksThresholds();
            if (metThreshold == attackThresholds[0]) {
                /* After 10 attacks, hoglins get regen 2 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // updates follow range
            } else if (metThreshold == attackThresholds[1]) {
                /* After 20 attacks, hoglins get regen 3 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            } else if (metThreshold == attackThresholds[2]) {
                /* After 40 attacks, hoglins summon a baby hoglin */
                CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
                newHoglin.a(true);
                new SpawnEntity(this.getWorld(), newHoglin, 1, null, null, this, false, true);
            } else if (metThreshold == attackThresholds[3]) {
                /* After 75 attacks, hoglins summon another baby hoglin */
                CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
                newHoglin.a(true);
                new SpawnEntity(this.getWorld(), newHoglin, 1, null, null, this, false, true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                     IGoalRemovingMob                                      //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initGoalRemovingMob() {
        this.vanillaTargetSelector = super.targetSelector;
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new CustomEntityHoglin.NewPathfinderGoalHoglinBreakRepellentBlocksAround(this, 20, 5, 1, 5, 1, false)); /* custom goal that breaks repellant blocks around the mob periodically */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target */
    }

    @Override
    public void die() {
        super.die();

        if (this.attackLevelingController != null && random.nextDouble() < (this.getAttacks() < 40 ? 0.3 : 1.0)) { /* hoglins have a 30% chance to spawn a zoglin after death (100% chance after 40 attacks) */
            new SpawnEntity(this.getWorld(), new CustomEntityZoglin(this.getWorld()), 1, null, null, this, false, true);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getGoalTarget() != null) {
            Location bukkitLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
            Block bukkitBlock = bukkitLoc.getBlock();

            if (bukkitBlock.getType() == org.bukkit.Material.AIR) { /* hoglins lay down fire trails on itself as long as it is inside an air block */
                bukkitBlock.setType(org.bukkit.Material.FIRE);
            }
        }
    }

    static class NewPathfinderGoalHoglinBreakRepellentBlocksAround extends NewPathfinderGoalBreakBlocksAround {

        public NewPathfinderGoalHoglinBreakRepellentBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
            super(entity, delayTimer, radX, radY, radZ, offsetY, removeFluids);
        }

        @Override
        public void e() {
            int X = (int) Math.floor(this.entity.getPositionVector().getX());
            int Y = (int) Math.floor(this.entity.getPositionVector().getY()) + offsetY;
            int Z = (int) Math.floor(this.entity.getPositionVector().getZ());
            org.bukkit.World bukkitWorld = this.entity.getWorld().getWorld();
            Block bukkitBlock;
            org.bukkit.Material bukkitMaterial;

            for (int x = -this.radX; x <= this.radX; x++) {
                for (int y = -this.radY; y <= this.radY; y++) {
                    for (int z = -this.radZ; z <= this.radZ; z++) {
                        int x1 = X + x, y1 = Y + y, z1 = Z + z;
                        bukkitBlock = bukkitWorld.getBlockAt(x1, y1, z1);
                        bukkitMaterial = bukkitBlock.getType();

                        if (bukkitMaterial == org.bukkit.Material.WARPED_FUNGUS || bukkitMaterial == org.bukkit.Material.POTTED_WARPED_FUNGUS || bukkitMaterial == org.bukkit.Material.NETHER_PORTAL || bukkitMaterial == org.bukkit.Material.RESPAWN_ANCHOR) {
                            bukkitBlock.setType(org.bukkit.Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
