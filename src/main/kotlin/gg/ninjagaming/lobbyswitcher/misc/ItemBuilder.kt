package gg.ninjagaming.lobbyswitcher.misc

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class ItemBuilder @JvmOverloads constructor(material: Material, amount: Int = 1) :
    ItemStack(material) {
    private val meta: ItemMeta?

    init {
        this.amount = amount
        this.meta = this.itemMeta
    }

    constructor(itemStack: ItemStack) : this(
        itemStack.type, itemStack.amount)

    fun setDisplayName(displayName: String?): ItemBuilder {
        this.meta?.setDisplayName(displayName)
        return this.build()
    }

    fun setLore(vararg lore: String?): ItemBuilder {
        this.meta?.lore = listOf(*lore)
        return this.build()
    }

    fun setLore(lore: MutableList<String>): ItemBuilder {
        for (i in lore.indices) lore[i] = ChatColor.translateAlternateColorCodes('&', lore[i])
        this.meta?.lore = lore
        return this.build()
    }

    fun addGlowEffect(): ItemBuilder {
        this.meta?.addEnchant(Enchantment.UNBREAKING, 1, true)
        this.meta?.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        return this.build()
    }

    fun build(): ItemBuilder {
        this.itemMeta = this.meta
        return this
    }
}
