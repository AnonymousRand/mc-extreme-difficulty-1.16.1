package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

public class RunnableSpawnBlocksEntitiesConstantly extends BukkitRunnable {

    private final EntityLiving entity;
    private final Material material;
    private final Entity firstEntityToSpawn;
    private Entity entityToBeSpawned;
    private final int xRadius, yRadius, zRadius;
    private final double yOffset;
    private final boolean terraform;
    private final World nmsWorld;
    protected int cycles;
    protected final int maxCycles;
    private CustomEntityAreaEffectCloud newAEC;
    private Location loc;

    public RunnableSpawnBlocksEntitiesConstantly(EntityLiving entity, @Nullable Material material, @Nullable Entity firstEntityToSpawn, int xRadius, int yRadius, int zRadius, double yOffset, boolean terraform) {
        this.entity = entity;
        this.material = material;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
        this.yOffset = yOffset;
        this.terraform = terraform;
        this.nmsWorld = entity.getWorld();
        this.cycles = 0;
        this.maxCycles = 1;
    }

    public RunnableSpawnBlocksEntitiesConstantly(EntityLiving entity, @Nullable Material material, @Nullable Entity firstEntityToSpawn, int xRadius, int yRadius, int zRadius, double yOffset, boolean terraform, int maxCycles) {
        this.entity = entity;
        this.material = material;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
        this.yOffset = yOffset;
        this.terraform = terraform;
        this.nmsWorld = entity.getWorld();
        this.cycles = 0;
        this.maxCycles = maxCycles;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        for (int x = -this.xRadius; x <= this.xRadius; x++) {
            for (int y = -this.yRadius; y <= this.yRadius; y++) {
                for (int z = -this.zRadius; z <= this.zRadius; z++) {
                    this.loc = new Location(this.nmsWorld.getWorld(), this.entity.locX() + x, this.entity.locY() + this.yOffset + y, this.entity.locZ() + z);

                    if (this.material != null) {
                        if (this.terraform) {
                            Material type = this.loc.getBlock().getType();

                            if (type != this.material && type != Material.AIR && type != Material.BEDROCK && type != Material.END_GATEWAY && type != Material.END_PORTAL && type != Material.END_PORTAL_FRAME && type != Material.NETHER_PORTAL && type != Material.OBSIDIAN && type != Material.CRYING_OBSIDIAN && type != Material.COMMAND_BLOCK && type != Material.COMMAND_BLOCK_MINECART && type != Material.STRUCTURE_BLOCK && type != Material.JIGSAW && type != Material.BARRIER && type != Material.SPAWNER && type != Material.COBWEB && type != Material.WATER && type != Material.LAVA && type != Material.FIRE) { // as long as it isn't one of these blocks) {
                                this.loc.getBlock().setType(this.material);

                                if (this.material == Material.COBWEB) {
                                    Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); // fire event that would otherwise not be fired so that the cobweb block can be broken after 2.5 seconds
                                }
                            }
                        } else {
                            if (this.loc.getBlock().getType() == org.bukkit.Material.AIR) {
                                this.loc.getBlock().setType(this.material);

                                if (this.material == Material.COBWEB || this.material == Material.SOUL_SOIL) {
                                    Bukkit.getPluginManager().callEvent(new BlockPlaceEvent(this.loc.getBlock(), this.loc.getBlock().getState(), null, null, null, false, null)); // fire event that would otherwise not be fired so that the cobweb or soul soil block can be broken after 2.5 seconds
                                }
                            }
                        }
                    } else if (this.firstEntityToSpawn != null) {
                        if (this.firstEntityToSpawn instanceof CustomEntityAreaEffectCloud) {
                            this.newAEC = (CustomEntityAreaEffectCloud)this.firstEntityToSpawn;

                            try {
                                this.entityToBeSpawned = this.firstEntityToSpawn.getClass().getDeclaredConstructor(World.class, float.class, int.class, int.class).newInstance(this.nmsWorld, this.newAEC.getRadius(), this.newAEC.getDuration(), this.newAEC.waitTime);
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            for (MobEffect effect : this.newAEC.effects) {
                                ((CustomEntityAreaEffectCloud) this.entityToBeSpawned).addEffect(effect);
                            }
                        } else if (this.firstEntityToSpawn instanceof EntityTNTPrimed) {
                            try {
                                this.entityToBeSpawned = this.firstEntityToSpawn.getClass().getDeclaredConstructor(EntityTypes.class, World.class).newInstance(EntityTypes.TNT, this.nmsWorld);
                            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        if (this.entityToBeSpawned != null) {
                            this.entityToBeSpawned.setPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ());
                            this.nmsWorld.addEntity(this.entityToBeSpawned);
                        }
                    }
                }
            }
        }
    }
}
