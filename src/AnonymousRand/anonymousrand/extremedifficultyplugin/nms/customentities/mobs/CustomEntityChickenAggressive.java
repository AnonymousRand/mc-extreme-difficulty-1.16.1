package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;

import java.lang.reflect.Field;
import java.util.Map;

// can't extend CustomEntityChicken as CustomEntityChicken has a function call in its tick() that spawns new aggressive chickens which would cause an infinite loop if we inherited from it
// todo if customentitychicken that thing moved to listener, extend it?
public class CustomEntityChickenAggressive extends EntityChicken implements ICustomHostile {
    
    /* Ignores y-level and line of sight for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private Field attributeMap;

    public CustomEntityChickenAggressive(World world) {
        super(EntityTypes.CHICKEN, world);
        this.initCustom();
    }

    private void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    private void initAttributes() {
        // pull attributeMap using reflection
        try {
            this.attributeMap = AttributeMapBase.class.getDeclaredField("b");
            this.attributeMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        // register new attributes using attributeMap
        try {
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // set new attributes
        /* Aggressive chickens do 2 damage and move twice as fast */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0);
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.5);
    }

    private void registerGenericAttribute(org.bukkit.entity.Entity entity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity) entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>) this.attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* Aggressive chickens have 16 block detection range */
    public double getDetectionRange() {
        return 16.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSq = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSqToNearestPlayer
                        > (double) randomDespawnDistSq && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        /* Aggressive chickens won't panic/breed/follow parent/be tempted with seeds */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this));                                                        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));                                                           /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMeleeAttack(this, 1.0));
        this.goalSelector.a(2, new NewPathfinderGoalPassiveMoveTowardsTarget(this, (float) this.getDetectionRange()));                /* Moves towards target, menacingly */
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
        /* Aggressive chickens attack these in decreasing priority: silverfish, endermites, other monsters, players, chickens, aggressive chickens, exploding aggressive chickens */
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntitySilverfish.class, false, false));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityEndermite.class, false, false));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityMonster.class, false, false));
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y));  /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodicaly retargets to the closest option */
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChicken.class, false, false));
        this.targetSelector.a(5, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChickenAggressive.class, false, false));
        this.targetSelector.a(6, new CustomPathfinderGoalNearestAttackableTarget<>(this, CustomEntityChickenAggressiveExploding.class, false, false));
        this.targetSelector.a(7, new CustomPathfinderGoalHurtByTarget(this, IGNORE_LOS, IGNORE_Y));                                   /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
    }

    @Override
    public void tick() {
        super.tick();
        Bukkit.broadcastMessage("aggressive chicken tick");

        /* Aggressive chickens die after 30 seconds */
        if (this.ticksLived == 600) {
            this.die();
        }
    }
}
