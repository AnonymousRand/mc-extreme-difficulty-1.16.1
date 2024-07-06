package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;

public class CustomEntityPufferfish extends EntityPufferFish implements ICustomHostile {

    private int lastStingTicks;
    private static Field jumpTicks;

    public CustomEntityPufferfish(World world) {
        super(EntityTypes.PUFFERFISH, world);
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.lastStingTicks = 0;
    }

    static {
        try {
            jumpTicks = EntityLiving.class.getDeclaredField("jumpTicks");
            jumpTicks.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* this mob now seeks out players; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void pickup(EntityHuman entityHuman) { // onCollideWithPlayer
        if (entityHuman.abilities.isInvulnerable) {
            return;
        }

        int i = this.getPuffState() + 1;

        if (!this.isSilent() && (this.ticksLived - this.lastStingTicks) > 100) {
            this.lastStingTicks = this.ticksLived; /* only plays sting sound once per 5 seconds */
            ((EntityPlayer) entityHuman).playerConnection.sendPacket(new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.j, 0.0F));
        }

        entityHuman.addEffect(new MobEffect(MobEffects.WITHER, 80 * i, 2)); /* poison from direct contact changed from poison 1 to wither 3, and duration increased from 50 ticks per puff state to 80 */
    }

    public double getFollowRange() { /* pufferfish have 32 block detection range */
        return 32.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
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
    public void tick() {
        super.tick();

        if (this.ticksLived % 3 == 0) {
            this.getWorld().getEntities(this, this.getBoundingBox().grow(5.0, 128.0, 5.0), entity -> entity instanceof EntityPlayer).forEach(entity -> this.pickup((EntityHuman) entity)); /* pufferfish have a poison/wither range of 5 blocks horizontally */
        }
    }

    @Override
    public void movementTick() { /* uses the movementick() method from entityLiving class so pufferfish no longer damage other mobs besides players */
        int jumpTicksTemp;

        try {
            jumpTicksTemp = jumpTicks.getInt(this);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        if (jumpTicksTemp > 0) {
            --jumpTicksTemp;
        }

        if (this.cr()) {
            this.bb = 0;
            this.c(this.locX(), this.locY(), this.locZ());
        }

        if (this.bb > 0) {
            double d0 = this.locX() + (this.bc - this.locX()) / (double) this.bb;
            double d1 = this.locY() + (this.bd - this.locY()) / (double) this.bb;
            double d2 = this.locZ() + (this.be - this.locZ()) / (double) this.bb;
            double d3 = MathHelper.g(this.bf - (double) this.yaw);

            this.yaw = (float) ((double) this.yaw + d3 / (double) this.bb);
            this.pitch = (float) ((double) this.pitch + (this.bg - (double) this.pitch) / (double) this.bb);
            --this.bb;
            this.setPosition(d0, d1, d2);
            this.setYawPitch(this.yaw, this.pitch);
        } else if (!this.doAITick()) {
            this.setMot(this.getMot().a(0.98D));
        }

        if (this.bi > 0) {
            this.aJ = (float) ((double) this.aJ + MathHelper.g(this.bh - (double) this.aJ) / (double) this.bi);
            --this.bi;
        }

        Vec3D vec3d = this.getMot();
        double d4 = vec3d.x;
        double d5 = vec3d.y;
        double d6 = vec3d.z;

        if (Math.abs(vec3d.x) < 0.003D) {
            d4 = 0.0D;
        }

        if (Math.abs(vec3d.y) < 0.003D) {
            d5 = 0.0D;
        }

        if (Math.abs(vec3d.z) < 0.003D) {
            d6 = 0.0D;
        }

        this.setMot(d4, d5, d6);
        this.getWorld().getMethodProfiler().enter("ai");
        if (this.isFrozen()) {
            this.jumping = false;
            this.aY = 0.0F;
            this.ba = 0.0F;
        } else if (this.doAITick()) {
            this.getWorld().getMethodProfiler().enter("newAi");
            this.doTick();
            this.getWorld().getMethodProfiler().exit();
        }

        this.getWorld().getMethodProfiler().exit();
        this.getWorld().getMethodProfiler().enter("jump");
        if (this.jumping && this.cS()) {
            double d7;

            if (this.aN()) {
                d7 = this.b(TagsFluid.LAVA);
            } else {
                d7 = this.b(TagsFluid.WATER);
            }

            boolean flag = this.isInWater() && d7 > 0.0D;
            double d8 = this.cw();

            if (flag && (!this.onGround || d7 > d8)) {
                this.c(TagsFluid.WATER);
            } else if (this.aN() && (!this.onGround || d7 > d8)) {
                this.c(TagsFluid.LAVA);
            } else if ((this.onGround || flag && d7 <= d8) && jumpTicksTemp == 0) {
                this.jump();
                jumpTicksTemp = 10;
            }
        } else {
            jumpTicksTemp = 0;
        }

        this.getWorld().getMethodProfiler().exit();
        this.getWorld().getMethodProfiler().enter("travel");
        this.aY *= 0.98F;
        this.ba *= 0.98F;
        this.t();
        AxisAlignedBB axisalignedbb = this.getBoundingBox();

        this.f(new Vec3D(this.aY, this.aZ, this.ba));
        this.getWorld().getMethodProfiler().exit();
        this.getWorld().getMethodProfiler().enter("push");
        if (this.bm > 0) {
            --this.bm;
            this.a(axisalignedbb, this.getBoundingBox());
        }

        this.collideNearby();

        try {
            jumpTicks.setInt(this, jumpTicksTemp);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.getWorld().getMethodProfiler().exit();
    }

    private void t() { // util method from entityLiving class
        boolean flag = this.getFlag(7);

        if (flag && !this.onGround && !this.isPassenger() && !this.hasEffect(MobEffects.LEVITATION)) {
            ItemStack itemstack = this.getEquipment(EnumItemSlot.CHEST);

            if (itemstack.getItem() == Items.ELYTRA && ItemElytra.d(itemstack)) {
                if (!this.getWorld().isClientSide && (this.bl + 1) % 20 == 0) {
                    itemstack.damage(1, this, (entityLiving)-> entityLiving.broadcastItemBreak(EnumItemSlot.CHEST));
                }
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }

        if (!this.getWorld().isClientSide) {
            this.setFlag(7, flag);
        }
    }
}
