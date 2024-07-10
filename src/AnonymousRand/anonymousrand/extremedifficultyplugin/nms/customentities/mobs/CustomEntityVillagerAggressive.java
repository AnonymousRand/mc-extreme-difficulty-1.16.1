package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_16_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;

import java.lang.reflect.Field;
import java.util.Map;

public class CustomEntityVillagerAggressive extends EntityVillager implements ICustomHostile {

    /* Ignores y-level and line of sight for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private static Field attributeMap;

    public CustomEntityVillagerAggressive(World world) {
        super(EntityTypes.VILLAGER, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        try { // register attack attributes
            registerGenericAttribute(this.getBukkitEntity(), Attribute.GENERIC_ATTACK_DAMAGE);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(12.0); /* aggressive villagers do 12 damage */
    }

    static {
        try {
            attributeMap = net.minecraft.server.v1_16_R1.AttributeMapBase.class.getDeclaredField("b");
            attributeMap.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void registerGenericAttribute(org.bukkit.entity.Entity entity, Attribute attribute) throws IllegalAccessException {
        AttributeMapBase attributeMapBase = ((CraftLivingEntity) entity).getHandle().getAttributeMap();
        Map<AttributeBase, AttributeModifiable> map = (Map<AttributeBase, AttributeModifiable>) attributeMap.get(attributeMapBase);
        AttributeBase attributeBase = CraftAttributeMap.toMinecraft(attribute);
        AttributeModifiable attributeModifiable = new AttributeModifiable(attributeBase, AttributeModifiable::getAttribute);
        map.put(attributeBase, attributeModifiable);
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder(); /* will still panic and flee if hit */
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMeleeAttack(this, 1.0));
        this.goalSelector.a(1, new NewPathfinderGoalPassiveMoveTowardsTarget(this, (float) this.getDetectionRange())); /* Moves towards target, menacingly */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the closest option */
    }

    public double getDetectionRange() { /* aggressive villagers have 40 block detection range */
        return 40.0;
    }
}
