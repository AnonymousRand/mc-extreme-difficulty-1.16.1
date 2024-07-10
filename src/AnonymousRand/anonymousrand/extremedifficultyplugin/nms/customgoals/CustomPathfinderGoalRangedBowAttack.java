package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomPathfinderGoalRangedBowAttack<T extends EntityMonster & IRangedEntity> extends PathfinderGoalBowShoot<T> {

    protected final T entity;
    protected final double speedTowardsTarget;
    protected int attackInterval;
    protected float maxAttackDistSq;
    protected int remainingAttackCooldown = -1;
    protected int seeTime;
    protected boolean strafingClockwise;
    protected boolean strafingBackwards;
    protected int strafingTime = -1;

    public CustomPathfinderGoalRangedBowAttack(T entity, double speedTowardsTarget, int attackInterval, float maxAttackDistance) {
        super(entity, speedTowardsTarget, attackInterval, maxAttackDistance);
        this.entity = entity;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackInterval = attackInterval;
        this.maxAttackDistSq = maxAttackDistance * maxAttackDistance;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    public void changeAttackInterval(int attackInterval) {
        this.attackInterval = attackInterval;
    }

    @Override
    public void e() {
        EntityLiving goalTarget = this.entity.getGoalTarget();

        if (goalTarget == null) {
            return;
        }
        double distanceToSquared = NMSUtil.distSq(this.entity, goalTarget, true);
        /* breaking line of sight does not stop the mob from attacking */
        ++this.seeTime;

        if (distanceToSquared <= (double) this.maxAttackDistSq && this.seeTime >= 20) {
            this.entity.getNavigation().o();
            ++this.strafingTime;
        } else {
            this.entity.getNavigation().a(goalTarget, this.speedTowardsTarget);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if ((double) this.entity.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double) this.entity.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distanceToSquared > (double) (this.maxAttackDistSq * 0.75F)) {
                this.strafingBackwards = false;
            } else if (distanceToSquared < (double) (this.maxAttackDistSq * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.entity.getControllerMove().a(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.entity.a(goalTarget, 30.0F, 30.0F);
        } else {
            this.entity.getControllerLook().a(goalTarget, 30.0F, 30.0F);
        }

        if (this.entity.isHandRaised()) {
            this.entity.clearActiveItem();
            this.entity.a(goalTarget, ItemBow.a(20)); // shoot(); ItemBow.a() gets the attack power for a corresponding charge of the bow in ticks (manually setting it to the normal 20 here to allow rapid fire, because normally this only runs if mob has charged bow for 20 ticks)
            this.remainingAttackCooldown = this.attackInterval;
        } else if (--this.remainingAttackCooldown <= 0 && this.seeTime >= -60) {
            this.entity.c(ProjectileHelper.a(this.entity, Items.BOW)); // startUsingItem()
        }
    }
}
