package tech.simonwalker.dimensionhop

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

object CustomItemBuilder {
    private val netherBottleNamespace = NamespacedKey(PortalBottlesPlugin.inst, "nether_bottle")
    private val endBottleNamespace = NamespacedKey(PortalBottlesPlugin.inst, "end_bottle")

    interface CustomItem {
        fun itemStack(): ItemStack
        fun matches(item: ItemStack): Boolean
        val recipe: Recipe
    }

    object NetherBottle: CustomItem {
        override fun itemStack() = ItemStack(Material.POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                color = Color.MAROON
//                basePotionData = PotionData(PotionType.FIRE_RESISTANCE)
                displayName(
                    Component.text("Bottle o' Nether")
                        .color(TextColor.color(181, 41, 16))
                        .decoration(TextDecoration.ITALIC, false)
                )
                lore(listOf(
                    Component.text("Like a nether portal in a bottle.")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                ))
                persistentDataContainer.set(netherBottleNamespace, PersistentDataType.BYTE, 1)
            }
        }

        override fun matches(item: ItemStack) = item.itemMeta.persistentDataContainer.has(netherBottleNamespace)

        override val recipe = ShapelessRecipe(netherBottleNamespace, itemStack()).apply {
            addIngredient(ItemStack(Material.POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply {
                    basePotionData = PotionData(PotionType.THICK)
                }
            })
            addIngredient(Material.CRYING_OBSIDIAN)
        }
    }

    object EndBottle: CustomItem {
        override fun itemStack() = ItemStack(Material.POTION).apply {
            itemMeta = (itemMeta as PotionMeta).apply {
                color = Color.BLACK
//                basePotionData = PotionData(PotionType.FIRE_RESISTANCE)
                displayName(
                    Component.text("End o' Bottle")
                        .color(TextColor.color(58, 21, 94))
                        .decoration(TextDecoration.ITALIC, false)
                )
                lore(listOf(
                    Component.text("Like an end portal in a bottle.")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
                ))
                persistentDataContainer.set(endBottleNamespace, PersistentDataType.BYTE, 1)
            }
        }

        override fun matches(item: ItemStack) = item.itemMeta.persistentDataContainer.has(endBottleNamespace)

        override val recipe = ShapelessRecipe(endBottleNamespace, itemStack()).apply {
            addIngredient(ItemStack(Material.POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply {
                    basePotionData = PotionData(PotionType.THICK)
                }
            })
            addIngredient(Material.DRAGON_HEAD)
        }
    }
}