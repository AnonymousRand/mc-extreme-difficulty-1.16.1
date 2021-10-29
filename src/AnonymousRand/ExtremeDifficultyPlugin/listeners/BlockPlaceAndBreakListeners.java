package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityGuardian;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySilverfish;
import net.minecraft.server.v1_16_R1.BlockWeb;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockPlaceAndBreakListeners implements Listener {

    private final JavaPlugin plugin;

    public BlockPlaceAndBreakListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Block bukkitBlock = event.getBlock();
        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        
        if (event.getPlayer() != null) {
            if (event.getPlayer().getLocation().getY() >= 129.0) { /**can't build above y level 128 in all dimensions to prevent towering up etc. to avoid mobs*/
                event.setCancelled(true);
                Bukkit.broadcastMessage("Not so fast, smartypants");
                Bukkit.broadcastMessage("You have reached the build height limit of 128 blocks :tf:");
            }

            if (bukkitBlock.getType() == Material.CONDUIT) { /**conduits spawn guardians every 5 seconds for 50 seconds*/
                Location loc = bukkitBlock.getLocation();
                new ConduitSummonGuardian(nmsWorld, loc, 10).runTaskTimer(this.plugin, 0L, 100L);
            }
        } else {
            if (bukkitBlock.getType() == Material.COBWEB || bukkitBlock.getType() == Material.LAVA) { /**spider-placed cobwebs and hoglin-placed lava is deleted after 4 seconds*/
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() //async thread is used so that the game doesn't pause completely for 10 seconds
                {
                    public void run()
                    {
                        bukkitBlock.setType(Material.AIR);
                    }
                }, 80);
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Block bukkitBlock = event.getBlock();
        Material type = bukkitBlock.getType();

        if (type == Material.SPAWNER || type == Material.CONDUIT || type == Material.STONE_BRICKS || type == Material.CRACKED_STONE_BRICKS || type == Material.MOSSY_STONE_BRICKS || type == Material.IRON_BARS || type == Material.STONE_BRICK_SLAB || type == Material.STONE_BRICK_STAIRS || type == Material.COBBLESTONE_STAIRS || type == Material.BOOKSHELF) { /**breaking these blocks (all found in strongholds) causes a silverfish to spawn*/
            World nmsWorld = ((CraftWorld)event.getPlayer().getWorld()).getHandle();

            for (int i = 0; i < (type == Material.CONDUIT ? 50 : (type == Material.SPAWNER ? 5 : 1)); i++) { /**breaking a spawner spawns 5 silverfish and breaking a conduit spawns 50*/
                CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(nmsWorld);
                newSilverfish.setPosition(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
                nmsWorld.addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.INFECTION);
            }
        }
    }

    @EventHandler
    public void blockExplodeAndBreakBlock(BlockExplodeEvent event) { /**explosions do not cause blocks to drop*/
        event.setYield(0.0f);

        World nmsWorld = ((CraftWorld)event.getBlock().getWorld()).getHandle();
        Material type;

        for (Block block : event.blockList()) {
            type = block.getType();

            if (type == Material.STONE_BRICKS || type == Material.CRACKED_STONE_BRICKS || type == Material.MOSSY_STONE_BRICKS || type == Material.IRON_BARS || type == Material.STONE_BRICK_SLAB || type == Material.STONE_BRICK_STAIRS || type == Material.COBBLESTONE_STAIRS || type == Material.BOOKSHELF) { /**breaking these blocks (all found in strongholds) causes a silverfish to spawn*/
                CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(nmsWorld);
                newSilverfish.setPosition(block.getX(), block.getY(), block.getZ());
                nmsWorld.addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.INFECTION);
            }
        }
    }

    @EventHandler
    public void entityExplodeAndBreakBlock(EntityExplodeEvent event) { /**explosions do not cause blocks to drop*/
        event.setYield(0.0f);

        World nmsWorld = ((CraftWorld)event.getEntity().getWorld()).getHandle();
        Material type;

        for (Block block : event.blockList()) {
            type = block.getType();

            if (type == Material.STONE_BRICKS || type == Material.CRACKED_STONE_BRICKS || type == Material.MOSSY_STONE_BRICKS || type == Material.IRON_BARS || type == Material.STONE_BRICK_SLAB || type == Material.STONE_BRICK_STAIRS || type == Material.COBBLESTONE_STAIRS || type == Material.BOOKSHELF) { /**breaking these blocks (all found in strongholds) causes a silverfish to spawn*/
                CustomEntitySilverfish newSilverfish = new CustomEntitySilverfish(nmsWorld);
                newSilverfish.setPosition(block.getX(), block.getY(), block.getZ());
                nmsWorld.addEntity(newSilverfish, CreatureSpawnEvent.SpawnReason.INFECTION);
            }
        }
    }

    static class ConduitSummonGuardian extends BukkitRunnable {

        private CustomEntityGuardian newGuardian;
        private final World nmsWorld;
        private final Location loc;
        private int cycles, maxCycles;

        public ConduitSummonGuardian(World nmsWorld, Location loc, int maxCycles) {
            this.nmsWorld = nmsWorld;
            this.loc = loc;
            this.cycles = 0;
            this.maxCycles = maxCycles;
        }

        @Override
        public void run() {
            if (++this.cycles >= this.maxCycles) {
                this.cancel();
            }

            this.newGuardian = new CustomEntityGuardian(this.nmsWorld);
            this.newGuardian.setPosition(this.loc.getX(), this.loc.getY(), this.loc.getZ());
            this.nmsWorld.addEntity(this.newGuardian);
        }
    }
}