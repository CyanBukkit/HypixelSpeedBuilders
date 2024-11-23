package cn.cyanbukkit.speed.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.util.*;

public class ItemBuilder {
    private static int dontStack = 0;
    private ItemStack is;

    public ItemBuilder(Material mat) {
        this.is = new ItemStack(mat);
    }


    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    public static ItemStack getCustomSkull(String base64, String name) {
        ItemStack miniWalls = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skull = (SkullMeta) miniWalls.getItemMeta();
        skull.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64));
        try {
            Field profileField = skull.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(skull, profile);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        miniWalls.setItemMeta(skull);
        return miniWalls;
    }

    public ItemBuilder material(Material mat) {
        this.is = new ItemStack(mat);
        return this;
    }

    public ItemBuilder setMaterial(Material material) {
        this.is.setType(material);
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.is.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = this.is.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        this.is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addRandomUnsafeEnchantment(int level, Enchantment... ench) {
        this.is.addUnsafeEnchantment(ench[(new Random()).nextInt(ench.length)], level);
        return this;
    }


    public ItemBuilder setUnbreakable() {
        ItemMeta im = this.is.getItemMeta();
        im.spigot().setUnbreakable(true);
        this.is.setItemMeta(im);
        return this;
    }


    public ItemBuilder dontStack() {
        dontStack++;
        return changeNbt("uuid", dontStack);
    }

    public ItemBuilder setLetherColor(Color color) {
        LeatherArmorMeta im = (LeatherArmorMeta) is.getItemMeta();
        im.setColor(color);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        SkullMeta im = (SkullMeta) is.getItemMeta();
        im.setOwner(owner);
        is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setSkullProperty(String texture) {
        SkullMeta meta = (SkullMeta) is.getItemMeta();
        GameProfile gp = new GameProfile(UUID.randomUUID(), null);
        gp.getProperties().put("textures", new Property("textures", texture));
        try {
            Field field = meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(meta, gp);
        } catch (Exception ignored) {
        }
        is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String name) {
        ItemMeta meta = this.is.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.add(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore);
        this.is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String... lore) {
        List<String> toSet = new ArrayList<>();
        ItemMeta meta = this.is.getItemMeta();

        for (String string : lore) {
            toSet.add(ChatColor.translateAlternateColorCodes('&', string));
        }

        meta.setLore(toSet);
        this.is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int i) {
        this.is.addUnsafeEnchantment(enchantment, i);
        return this;
    }

    public ItemBuilder prefix(String prefix) {
        return this.changeNbt("prefix", prefix);
    }

    public ItemBuilder lore(List<String> lore) {
        List<String> toSet = new ArrayList<>();
        ItemMeta meta = this.is.getItemMeta();

        for (String string : lore) {
            toSet.add(ChatColor.translateAlternateColorCodes('&', string));
        }

        meta.setLore(toSet);
        this.is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder durability(int durability) {
        this.is.setDurability((short) durability);
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        this.is.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment) {
        this.is.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder enchantments(Map<Enchantment, Integer> enchantments) {
        this.is.addUnsafeEnchantments(enchantments);
        return this;
    }

    public ItemBuilder glow() {
        return this.enchant(Enchantment.LURE, 1).flags(ItemFlag.values());
    }

    public ItemBuilder flags(ItemFlag... flags) {
        ItemMeta itemMeta = this.is.getItemMeta();
        itemMeta.addItemFlags(flags);
        this.is.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder type(Material material) {
        this.is.setType(material);
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = this.is.getItemMeta();
        meta.setLore(new ArrayList<>());
        this.is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearEnchantments() {

        for (Enchantment e : this.is.getEnchantments().keySet()) {
            this.is.removeEnchantment(e);
        }

        return this;
    }

    public ItemBuilder canDrop(boolean allow) {
        this.changeNbt("tradeAllow", allow);
        return this;
    }

    public ItemBuilder isHealingItem(boolean isHealingItem) {
        this.changeNbt("isHealingItem", isHealingItem);
        return this;
    }

    public ItemBuilder canTrade(boolean allow) {
        this.changeNbt("canTrade", allow);
        return this;
    }

    public ItemBuilder forceCanTrade(boolean allow) {
        this.changeNbt("forceCanTrade", allow);
        return this;
    }

    public ItemBuilder defaultItem() {
        this.changeNbt("defaultItem", true);
        return this;
    }

    public ItemBuilder canSaveToEnderChest(boolean allow) {
        this.changeNbt("enderChest", allow);
        return this;
    }

    public ItemBuilder changeNbt(String key, String value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        NBTTagCompound extra = tag.getCompound("extra");
        if (extra == null) {
            extra = new NBTTagCompound();
        }
        extra.setString(key, value);
        tag.set("extra", extra);

        nmsItem.setTag(tag);

        this.is = CraftItemStack.asBukkitCopy(nmsItem);

        return this;
    }

    public ItemBuilder changeNbt(String key, boolean value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        NBTTagCompound extra = tag.getCompound("extra");
        if (extra == null) {
            extra = new NBTTagCompound();
        }
        extra.setBoolean(key, value);
        tag.set("extra", extra);

        nmsItem.setTag(tag);

        this.is = CraftItemStack.asBukkitCopy(nmsItem);

        return this;
    }

    public ItemBuilder changeNbt(String key, int value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        NBTTagCompound extra = tag.getCompound("extra");
        if (extra == null) {
            extra = new NBTTagCompound();
        }
        extra.setInt(key, value);
        tag.set("extra", extra);

        nmsItem.setTag(tag);

        this.is = CraftItemStack.asBukkitCopy(nmsItem);

        return this;
    }

    public ItemBuilder changeNbt(String key, double value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }
        NBTTagCompound extra = tag.getCompound("extra");
        if (extra == null) {
            extra = new NBTTagCompound();
        }
        extra.setDouble(key, value);
        tag.set("extra", extra);

        nmsItem.setTag(tag);

        this.is = CraftItemStack.asBukkitCopy(nmsItem);

        return this;
    }

    public ItemBuilder unbreakable(boolean b, boolean show) {
        if (show) {
            ItemMeta itemMeta = is.getItemMeta();
            itemMeta.spigot().setUnbreakable(true);
            is.setItemMeta(itemMeta);
            return this;
        } else {
            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            tag.setBoolean("Unbreakable", b);
            return this;
        }
    }

    public ItemBuilder internalName(String name) {
        this.changeNbt("internal", name);
        return this;
    }

    public ItemBuilder removeOnJoin(boolean remove) {
        this.changeNbt("removeOnJoin", remove);
        return this;
    }

    public ItemStack build() {
        return is;
    }

    public ItemBuilder deathDrop(boolean drop) {
        this.changeNbt("deathDrop", drop);
        return this;
    }


    public ItemBuilder maxLive(int live) {
        this.changeNbt("maxLive", live);
        return this;
    }

    public ItemBuilder kills(int kills) {
        this.changeNbt("kills", kills);
        return this;
    }

    public ItemBuilder dyeColor(DyeColor color) {
        this.changeNbt("dyeColor", color.name());
        return this;
    }

    public ItemBuilder dyeColor(String color) {
        this.changeNbt("dyeColor", color);
        return this;
    }

    public ItemBuilder tier(int tier) {
        this.changeNbt("tier", tier);
        return this;
    }

    public ItemBuilder live(int live) {
        this.changeNbt("live", live);
        return this;
    }

    public ItemBuilder itemDamage(double damageValue) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(is);
        NBTTagCompound tag = nmsItem.getTag();
        if (tag == null) {
            tag = new NBTTagCompound();
        }

        NBTTagList modifiers = new NBTTagList();
        NBTTagCompound damage = new NBTTagCompound();
        damage.set("AttributeName", new NBTTagString("generic.attackDamage"));
        damage.set("Name", new NBTTagString("generic.attackDamage"));
        damage.set("Amount", new NBTTagDouble(damageValue));
        damage.set("Operation", new NBTTagInt(0));
        damage.set("UUIDLeast", new NBTTagInt(894654));
        damage.set("UUIDMost", new NBTTagInt(2872));
        modifiers.add(damage);
        tag.set("AttributeModifiers", modifiers);
        nmsItem.setTag(tag);

        this.is = CraftItemStack.asBukkitCopy(nmsItem);

        return this;
    }

    public ItemBuilder addPotionEffect(PotionEffect effect, boolean b) {
        if (this.is.getItemMeta() instanceof PotionMeta) {
            final PotionMeta meta = (PotionMeta) this.is.getItemMeta();
            meta.addCustomEffect(effect, b);
            this.is.setItemMeta(meta);
        }

        return this;
    }
}
