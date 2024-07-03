package AnonymousRand.anonymousrand.extremedifficultyplugin;

import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.BlockOverride;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import net.minecraft.server.v1_16_R1.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtremeDifficultyPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        this.changeBlocksBlastResistance();
        CustomMathHelper.initTrigTables();
    }

    @Override
    public void onEnable() { // this runs when the plugin is first enabled (when the server starts up)
        this.addEyeOfEnderRecipe();
        this.getCommand("explosionvolume").setExecutor(new CommandExplosionVolume()); /** run "/explosionvolume [set/get] [decimal]" to set the volume of explosions */
        this.initializeExplosionVolume();
        this.initializeListeners();
        this.initializePluginFields();
    }

    @Override
    public void onDisable() {

    }

    private void addEyeOfEnderRecipe() { /** changes eye of ender recipe */
        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft("ender_eye"));
        NamespacedKey key = new NamespacedKey(this, "eye_of_ender");
        ShapelessRecipe newRecipe = new ShapelessRecipe(key, new ItemStack(Material.ENDER_EYE));
        newRecipe.addIngredient(Material.BLAZE_ROD);
        newRecipe.addIngredient(Material.ENDER_PEARL);
        newRecipe.addIngredient(Material.NETHERITE_HOE);
        newRecipe.addIngredient(Material.BEETROOT_SOUP);
        newRecipe.addIngredient(Material.SCUTE);
        newRecipe.addIngredient(Material.WRITABLE_BOOK);
        newRecipe.addIngredient(Material.DAYLIGHT_DETECTOR);
        newRecipe.addIngredient(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        newRecipe.addIngredient(Material.DEAD_BRAIN_CORAL);
        Bukkit.getServer().addRecipe(newRecipe);
    }

    private void changeBlocksBlastResistance() {
        BlockOverride endStone = new BlockOverride(Blocks.END_STONE); /** end stone now has a blast resistance of 16 */
        endStone.set("durability", 16.0F);

        BlockOverride obsidian = new BlockOverride(Blocks.OBSIDIAN); /** obsidian, crying obsidian, anvils, enchanting tables, ancient debris and respawn anchors now has a blast resistance of 6, the same as cobblestone */
        obsidian.set("durability", 6.0F);

        BlockOverride cryingObsidian = new BlockOverride(Blocks.CRYING_OBSIDIAN);
        cryingObsidian.set("durability", 6.0F);

        BlockOverride anvil = new BlockOverride(Blocks.ANVIL);
        anvil.set("durability", 6.0F);

        BlockOverride enchantingTable = new BlockOverride(Blocks.ENCHANTING_TABLE);
        enchantingTable.set("durability", 6.0F);

        BlockOverride ancientDebris = new BlockOverride(Blocks.ANCIENT_DEBRIS);
        ancientDebris.set("durability", 6.0F);

        BlockOverride respawnAnchor = new BlockOverride(Blocks.RESPAWN_ANCHOR);
        respawnAnchor.set("durability", 6.0F);

        BlockOverride spawner = new BlockOverride(Blocks.SPAWNER); /** spawners are now indestructible by explosions */
        spawner.set("durability", 3600000.0F);

        BlockOverride conduit = new BlockOverride(Blocks.CONDUIT); /** conduits are now indestructible by explosions */
        conduit.set("durability", 3600000.0F);
    }

    private void initializeExplosionVolume() { // initializes static explosion volume
        ListenerPlayerJoinAndQuit.explosionVolumeMultiplier = 1.0;
        ListenerPlayerJoinAndQuit.firstExplosion = true;
    }

    private void initializeListeners() { // registers the listeners
        getServer().getPluginManager().registerEvents(new ListenerBlockPlaceAndBreak(), this);
        getServer().getPluginManager().registerEvents(new ListenerDragonFight(), this);
        getServer().getPluginManager().registerEvents(new ListenerDropItem(),this);
        getServer().getPluginManager().registerEvents(new ListenerEggs(), this);
        getServer().getPluginManager().registerEvents(new ListenerMobDamage(), this);
        getServer().getPluginManager().registerEvents(new ListenerMobDeath(),this);
        getServer().getPluginManager().registerEvents(new ListenerMobSpawnAndReplaceWithCustom(), this);
        getServer().getPluginManager().registerEvents(new ListenerLightningStrike(),this);
        getServer().getPluginManager().registerEvents(new ListenerPiglinBarter(), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerDamage(),this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerDeathAndRespawn(), this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerEat(),this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerInteract(),this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerJoinAndQuit(),this);
        getServer().getPluginManager().registerEvents(new ListenerPlayerMovementAndFallDamage(), this);
        getServer().getPluginManager().registerEvents(new ListenerPotionEffect(), this);
        getServer().getPluginManager().registerEvents(new ListenerProjectile(), this);
        getServer().getPluginManager().registerEvents(new ListenerRaidAndVillager(),this);
        getServer().getPluginManager().registerEvents(new ListenerSheepDye(), this);
        getServer().getPluginManager().registerEvents(new ListenerSleep(), this);
        getServer().getPluginManager().registerEvents(new ListenerVehicleCreate(), this);
    }

    private void initializePluginFields() { // initializes static plugin fields
        StaticPlugin.plugin = this;
    }
}
