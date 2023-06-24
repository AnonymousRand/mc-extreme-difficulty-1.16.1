package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.EntityZombie;

public class CustomPathfinderGoalZombieAttack extends CustomPathfinderGoalMeleeAttack {

    private final EntityZombie zombie;
    private int raiseArmTicks;

    public CustomPathfinderGoalZombieAttack(EntityZombie entityZombie, double speedTowardsTarget) {
        super(entityZombie, speedTowardsTarget);
        this.zombie = entityZombie;
    }

    @Override
    public void c() {
        super.c();
        this.raiseArmTicks = 0;
    }

    @Override
    public void d() {
        super.d();
        this.zombie.setAggressive(false);
    }

    @Override
    public void e() {
        super.e();
        ++this.raiseArmTicks;

        if (this.raiseArmTicks >= 5 && this.j() < this.k() / 2) {
            this.zombie.setAggressive(true);
        } else {
            this.zombie.setAggressive(false);
        }

    }
}
