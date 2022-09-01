package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

public class NewPathfinderGoalUpgradeArmor extends PathfinderGoal {

    private final EntityInsentient nmsEntity;
    private final LivingEntity bukkitEntity;
    private EntityEquipment bukkitEquipment;
    private HashMap<Integer, Boolean> attackBooleans = new HashMap<>();
    private Field attacks;

    public NewPathfinderGoalUpgradeArmor(EntityInsentient nmsEntity) {
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = ((LivingEntity)this.nmsEntity.getBukkitEntity());
        this.attackBooleans.put(4, false);
        this.attackBooleans.put(8, false);
        this.attackBooleans.put(11, false);
        this.attackBooleans.put(15, false);
        this.attackBooleans.put(19, false);
        this.attackBooleans.put(22, false);
        this.attackBooleans.put(26, false);
        this.attackBooleans.put(29, false);
        this.attackBooleans.put(32, false);
        this.attackBooleans.put(36, false);
        this.attackBooleans.put(39, false);
        this.attackBooleans.put(42, false);
        this.attackBooleans.put(50, false);
        Arrays.fill(this.nmsEntity.dropChanceArmor, 0.0f);  /** entity can't drop the items in their armor slots */

        try {
            this.attacks = this.nmsEntity.getClass().getDeclaredField("attacks");
            this.attacks.setAccessible(true);
        } catch (NoSuchFieldException e) {}
    }

    @Override
    public boolean a() {
        int attacksLocal;

        try {
            attacksLocal = this.attacks.getInt(this.nmsEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        if (this.attackBooleans.containsKey(attacksLocal)) {
            if (!this.attackBooleans.get(attacksLocal)) {
                this.attackBooleans.replace(attacksLocal, true);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        int attacksLocal;
        this.bukkitEquipment = this.bukkitEntity.getEquipment();

        try {
            attacksLocal = this.attacks.getInt(this.nmsEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        switch (attacksLocal) {
            case 4 -> {
                if (this.bukkitEquipment.getChestplate().getType() != org.bukkit.Material.IRON_CHESTPLATE) {
                    this.bukkitEquipment.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
                }
            }
            case 8 -> {
                if (this.bukkitEquipment.getLeggings().getType() != org.bukkit.Material.IRON_LEGGINGS) {
                    this.bukkitEquipment.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_LEGGINGS));
                }
            }
            case 11 -> {
                org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /** most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);

                if (this.bukkitEquipment.getHelmet().getType() != org.bukkit.Material.IRON_HELMET) {
                    this.bukkitEquipment.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HELMET));
                }

                if (this.bukkitEquipment.getBoots().getType() != org.bukkit.Material.IRON_BOOTS) {
                    this.bukkitEquipment.setBoots(boots);
                }
            }
            case 15 -> {
                if (this.bukkitEquipment.getChestplate().getType() != org.bukkit.Material.DIAMOND_CHESTPLATE) {
                    this.bukkitEquipment.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE));
                }
            }
            case 19 -> {
                if (this.bukkitEquipment.getLeggings().getType() != org.bukkit.Material.DIAMOND_LEGGINGS) {
                    this.bukkitEquipment.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_LEGGINGS));
                }
            }
            case 22 -> {
                org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /** most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);

                if (this.bukkitEquipment.getHelmet().getType() != org.bukkit.Material.DIAMOND_HELMET) {
                    this.bukkitEquipment.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));
                }

                if (this.bukkitEquipment.getBoots().getType() != org.bukkit.Material.DIAMOND_BOOTS) {
                    this.bukkitEquipment.setBoots(boots);
                }
            }
            case 26 -> {
                org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);

                if (this.bukkitEquipment.getChestplate().getType() != org.bukkit.Material.NETHERITE_CHESTPLATE) {
                    this.bukkitEquipment.setChestplate(chestplate);
                }
            }
            case 29 -> {
                org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);

                if (this.bukkitEquipment.getLeggings().getType() != org.bukkit.Material.NETHERITE_LEGGINGS) {
                    this.bukkitEquipment.setLeggings(leggings);
                }
            }
            case 32 -> {
                org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);

                org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /** most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);

                if (this.bukkitEquipment.getHelmet().getType() != org.bukkit.Material.NETHERITE_HELMET) {
                    this.bukkitEquipment.setHelmet(helmet);
                }

                if (this.bukkitEquipment.getBoots().getType() != org.bukkit.Material.NETHERITE_BOOTS) {
                    this.bukkitEquipment.setBoots(boots);
                }
            }
            case 36 -> {
                org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                if (this.bukkitEquipment.getChestplate().getType() != org.bukkit.Material.NETHERITE_CHESTPLATE) {
                    this.bukkitEquipment.setChestplate(chestplate);
                }
            }
            case 39 -> {
                org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                if (this.bukkitEquipment.getLeggings().getType() != org.bukkit.Material.NETHERITE_LEGGINGS) {
                    this.bukkitEquipment.setLeggings(leggings);
                }
            }
            case 42 -> {
                org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /** most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);

                if (this.bukkitEquipment.getHelmet().getType() != org.bukkit.Material.NETHERITE_HELMET) {
                    this.bukkitEquipment.setHelmet(helmet);
                }

                if (this.bukkitEquipment.getBoots().getType() != org.bukkit.Material.NETHERITE_BOOTS) {
                    this.bukkitEquipment.setBoots(boots);
                }
            }
            case 50 -> {
                org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);

                if (this.bukkitEquipment.getChestplate().getType() != org.bukkit.Material.NETHERITE_CHESTPLATE) {
                    this.bukkitEquipment.setChestplate(chestplate);
                }

                org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);

                if (this.bukkitEquipment.getLeggings().getType() != org.bukkit.Material.NETHERITE_LEGGINGS) {
                    this.bukkitEquipment.setLeggings(leggings);
                }

                org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);

                org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /** most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                if (this.bukkitEquipment.getHelmet().getType() != org.bukkit.Material.NETHERITE_HELMET) {
                    this.bukkitEquipment.setHelmet(helmet);
                }

                if (this.bukkitEquipment.getBoots().getType() != org.bukkit.Material.NETHERITE_BOOTS) {
                    this.bukkitEquipment.setBoots(boots);
                }
            }
        }
    }
}
