package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomEntityBat extends EntityBat implements ICustomMob {

    public int attacks;
    private boolean a3, a7, a12, a24, a32, firstDuplicate;
    private BlockPosition d;
    private NewPathfinderGoalBuffMobs buffMobs = new NewPathfinderGoalBuffMobs(this, EntityInsentient.class, this.buildBuffsHashmap(), 32, 3, 200, 101);
    private static final CustomPathfinderTargetCondition c = (new CustomPathfinderTargetCondition()).a(4.0D).b();
    private static Field attributeMap;

    public CustomEntityBat(World world) { /** bats are now aggressive */
        super(EntityTypes.BAT, world);
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a3 = false;
        this.a7 = false;
        this.a12 = false;
        this.a24 = false;
        this.a32 = false;
        this.firstDuplicate = true;

        try { // register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_KNOCKBACK);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1.0); /** bats do 1 damage and have extra knockback */
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(2.0);
        this.goalSelector.a(0, this.buffMobs); /** custom goal that provides the buffing mechanism */
    }

    // registers new attributes via reflection; code from Spigot forums
    static {
        try {
            attributeMap = AttributeMapBase.class.getDeclaredField("b");
            attributeMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void registerGenericAttribute(org.bukkit.entity.Entity entity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity)entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>)attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    @Override
    public void initPathfinder() {
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMeleeAttack(this, 1.0, false)); /** uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level */
    }

    protected HashMap<Integer, ArrayList<MobEffect>> buildBuffsHashmap() { /** buffs: after 3 attacks, all mobs within 32 block sphere get speed 1, strength 1, and regen 2 for 4 minutes. After 12 attacks, all mobs within 64 block sphere get strength 2 and shoot an arrow every 20 ticks. After 24 attacks, all mobs within 64 block sphere shoot an arrow every 14 ticks and spawn a silverfish every 12 seconds. After 32 attacks, all mobs within 64 block sphere get regen 3 for 4 minutes and shoot an arrow every 8 ticks */
        HashMap<Integer, ArrayList<MobEffect>> buffs = new HashMap<>();

        ArrayList<MobEffect> attacks3 = new ArrayList<>();
        ArrayList<MobEffect> attacks12 = new ArrayList<>();
        ArrayList<MobEffect> attacks24 = new ArrayList<>();
        ArrayList<MobEffect> attacks32 = new ArrayList<>();

        attacks3.add(new MobEffect(MobEffects.REGENERATION, 4800, 1));
        attacks3.add(new MobEffect(MobEffects.FASTER_MOVEMENT, 4800, 0));
        attacks3.add(new MobEffect(MobEffects.INCREASE_DAMAGE, 4800, 0));
        attacks12.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 252));
        attacks12.add(new MobEffect(MobEffects.INCREASE_DAMAGE, 4800, 1));
        attacks12.add(new MobEffect(MobEffects.REGENERATION, 4800, 2));
        attacks24.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 253));
        attacks32.add(new MobEffect(MobEffects.HUNGER, Integer.MAX_VALUE, 254));

        buffs.put(3, attacks3);
        buffs.put(12, attacks12);
        buffs.put(24, attacks24);
        buffs.put(32, attacks32);

        return buffs;
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.firstDuplicate) { /** summons 10-15 bats when hit by player and not killed for the first time (also 2 aggressive bats after 32 attacks) */
            this.firstDuplicate = false;
            new SpawnEntity(this.getWorld(), new EntityBat(EntityTypes.BAT, this.getWorld()), random.nextInt(6) + 10, CreatureSpawnEvent.SpawnReason.DROWNED, null, this, false, false);

            if (this.attacks >= 32) {
                new SpawnEntity(this.getWorld(), new CustomEntityBat(this.getWorld()), 2, null, null, this, false, false);
            }
        }

        return super.damageEntity(damagesource, f);
    }

    @Override
    protected void mobTick() {
        BlockPosition blockposition = this.getChunkCoordinates();
        BlockPosition blockposition1 = blockposition.up();

        if (this.isAsleep()) {
            boolean flag = this.isSilent();

            if (this.getWorld().getType(blockposition1).isOccluding(this.getWorld(), blockposition)) {
                if (random.nextInt(200) == 0) {
                    this.aJ = (float) random.nextInt(360);
                }

                if (this.getWorld().a(c, (EntityLiving) this) != null) {
                    this.setAsleep(false);
                    if (!flag) {
                        this.getWorld().a((EntityHuman) null, 1025, blockposition, 0);
                    }
                }
            } else {
                this.setAsleep(false);
                if (!flag) {
                    this.getWorld().a((EntityHuman) null, 1025, blockposition, 0);
                }
            }
        } else {
            if (this.d != null && (!this.getWorld().isEmpty(this.d) || this.d.getY() < 1)) {
                this.d = null;
            }

            if (this.ticksLived % 3 == 0) { // updates path every 3 ticks
                this.d = null;
            }

            if (this.d == null && this.getGoalTarget() != null) { /** always flies towards goal target if possible; pathfinder goals and navigator doesn't work because bats' movement doesn't follow them, only this method */
                this.d = new BlockPosition(this.getGoalTarget().locX(), this.getGoalTarget().locY(), this.getGoalTarget().locZ());
            } else if (d == null) {
                this.d = new BlockPosition(this.locX() + (double) random.nextInt(7) - (double) random.nextInt(7), this.locY() + (double) random.nextInt(6) - 2.0D, this.locZ() + (double) random.nextInt(7) - (double) random.nextInt(7));
            }

            double d0 = (double) this.d.getX() + 0.5D - this.locX();
            double d1 = (double) this.d.getY() + 0.1D - this.locY();
            double d2 = (double) this.d.getZ() + 0.5D - this.locZ();
            Vec3D vec3d = this.getMot();
            Vec3D vec3d1 = vec3d.add((Math.signum(d0) * 0.5D - vec3d.x) * 0.10000000149011612D, (Math.signum(d1) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D, (Math.signum(d2) * 0.5D - vec3d.z) * 0.10000000149011612D);

            this.setMot(vec3d1);
            float f = (float)(MathHelper.d(vec3d1.z, vec3d1.x) * 57.2957763671875D) - 90.0F;
            float f1 = MathHelper.g(f - this.yaw);

            this.ba = 0.5F;
            this.yaw += f1;
        }
}

    public double getFollowRange() { /** bats have 16 block detection range (setting attribute doesn't work) (24 after 5 attacks, 32 after 10 attacks) */
        return this.attacks < 5 ? 16.0 : this.attacks < 10 ? 24 : 32;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 3 && !this.a3) {
            this.a3 = true;
            buffMobs.e(); /** buffs are immediately applied the first time */
        }

        if (this.attacks == 7 && !this.a7) { /** after 7 attacks, bats gain regen 2, speed 1, and 12 max health and health */
            this.a7 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.0);
            this.setHealth(12.0F);
        }

        if (this.attacks == 12 && !this.a12) { /** after 12 attacks, bats gain speed 2 and 15 max health and health */
            this.a12 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 1));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(15.0);
            this.setHealth(15.0F);

            this.goalSelector.a(this.buffMobs); // remove goal and replace
            this.buffMobs = new NewPathfinderGoalBuffMobs(this, EntityLiving.class, this.buildBuffsHashmap(), 64, 20, 200, 101);
            this.goalSelector.a(0, this.buffMobs);
            this.buffMobs.e(); /** buffs are immediately applied the first time */
        }

        if (this.attacks == 24 && !this.a24) {
            this.a24 = true;
            this.buffMobs.e(); /** buffs are immediately applied the first time */
        }

        if (this.attacks == 32 && !this.a32) { /** bats can duplicate again one time after 45 attacks */
            this.a32 = true;
            this.firstDuplicate = true;
            this.buffMobs.e(); /** buffs are immediately applied the first time */
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this); */
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
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter sometimes, eg. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        if (random.nextDouble() < 0.1) {
            return d3 * d3 + Math.pow(this.locY() - d1, 2) + d5 * d5;
        } else {
            return d3 * d3 + d5 * d5;
        }
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter sometimes, eg. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        if (random.nextDouble() < 0.1) {
            return d0 * d0 + Math.pow(this.locY() - vec3d.y, 2) + d2 * d2;
        } else {
            return d0 * d0 + d2 * d2;
        }
    }
}
