package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomEntityZombieSuper extends EntityZombie {

    private final JavaPlugin plugin;
    public PathfinderGoalSelector targetSelectorVanilla;

    public CustomEntityZombieSuper(World world, JavaPlugin plugin) {
        super(EntityTypes.ZOMBIE, world);
        this.plugin = plugin;
        this.targetSelectorVanilla = super.targetSelector;
    }

    @Override
    protected void initPathfinder() { /**no longer targets iron golems*/
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 2, 1, 2, 1, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) {
            EntityLiving entityliving = this.getGoalTarget();

            if (entityliving == null) {
                entityliving = (EntityLiving) damagesource.getEntity();
            }

            if (!(entityliving instanceof EntityPlayer)) { /**only player damage can trigger reinforcement spawning*/
                return true;
            }

            if ((double)this.random.nextFloat() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /**zombies can now spawn reinforcements on any difficulty*/
                for (int ii = 0; ii < (this.random.nextDouble() < 0.99 ? 1 : 30); ii++) { /**1% chance to make 30 spawn attempts instead of 1 (on average, about half of them succeed)*/
                    int i = MathHelper.floor(this.locX());
                    int j = MathHelper.floor(this.locY());
                    int k = MathHelper.floor(this.locZ());
                    CustomEntityZombie newZombie = new CustomEntityZombie(this.getWorld(), this.plugin);

                    for (int l = 0; l < 50; ++l) {
                        int i1 = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                        int j1 = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                        int k1 = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                        BlockPosition blockposition = new BlockPosition(i1, j1, k1);
                        EntityTypes<?> entitytypes = newZombie.getEntityType();
                        EntityPositionTypes.Surface entitypositiontypes_surface = EntityPositionTypes.a(entitytypes);

                        if (SpawnerCreature.a(entitypositiontypes_surface, (IWorldReader) this.getWorld(), blockposition, entitytypes) && EntityPositionTypes.a(entitytypes, this.getWorld(), EnumMobSpawn.REINFORCEMENT, blockposition, this.getWorld().random)) {
                            newZombie.setPosition((double) i1, (double) j1, (double) k1);
                            if (!this.getWorld().isPlayerNearby((double) i1, (double) j1, (double) k1, 7.0D) && this.getWorld().i(newZombie) && this.getWorld().getCubes(newZombie) && !this.getWorld().containsLiquid(newZombie.getBoundingBox())) {
                                this.getWorld().addEntity(newZombie);
                                newZombie.prepare(this.getWorld(), this.getWorld().getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, (GroupDataEntity) null, (NBTTagCompound) null);
                                this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125, AttributeModifier.Operation.ADDITION)); /**zombies experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement (15% after 7 attacks)*/
                                newZombie.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.125, AttributeModifier.Operation.ADDITION));
                                break;
                            }
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public double getFollowRange() { /**super zombies have 128 block detection range (setting attribute doesn't work)*/
        return 128.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /**doesn't convert to drowned in water*/
            this.drownedConversionTime = Integer.MAX_VALUE;
        }

        if (this.ticksLived == 10) { /**super zombies move 3x faster, always summon a reinforcement when hit, and have 40 health*/
            this.setBaby(false);
            this.setCanPickupLoot(true);
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.69);
            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(1.0);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(40.0);
            this.setHealth(40.0F);
            RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
        }
    }
}