package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.EntityHuman;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public interface CustomIEntityAccess {

    default EntityHuman customFindPlayers(CustomPathfinderTargetCondition pathfindertargetcondition, EntityLiving entityliving, double d0, double d1, double d2) { //passes to customFindEntities()
        return (EntityHuman)this.customFindEntities(entityliving.world.getPlayers(), pathfindertargetcondition, entityliving, d0, d1, d2);
    }

    default <T extends EntityLiving> EntityLiving customFindEntities(List<? extends T> list, CustomPathfinderTargetCondition pathfindertargetcondition, @Nullable EntityLiving entityliving, double d0, double d1, double d2) {
        double d3 = -1.0D;
        EntityLiving t0 = null;
        Iterator iterator = list.iterator();

        while (iterator.hasNext()) {
            EntityLiving t1 = (EntityLiving) iterator.next();

            if (pathfindertargetcondition.a(entityliving, t1)) { //uses the overriden a() function in the CustomPathfinderTargetCondition class which turns off line of sight requirement to initially find a target player
                double d4 = t1.g(d0, d1, d2);

                if (d3 == -1.0D || d4 < d3) {
                    d3 = d4;
                    t0 = t1;
                }
            }
        }

        return t0;
    }
}
