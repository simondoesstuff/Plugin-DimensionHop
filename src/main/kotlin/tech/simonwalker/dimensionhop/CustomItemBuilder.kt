package tech.simonwalker.dimensionhop

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

object CustomItemBuilder {
    val netherBottleNamespace = NamespacedKey(DimensionHopPlugin.inst, "nether_bottle")
    val endBottleNamespace = NamespacedKey(DimensionHopPlugin.inst, "end_bottle")

    object NetherBottle {
        fun itemStack() = ItemStack(Material.POTION).apply {
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

        val recipe = ShapelessRecipe(netherBottleNamespace, itemStack()).apply {
            addIngredient(ItemStack(Material.POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply {
                    basePotionData = PotionData(PotionType.AWKWARD)
                }
            })
            addIngredient(Material.CRYING_OBSIDIAN)
        }
    }

    object EndBottle {
        fun itemStack() = ItemStack(Material.POTION).apply {
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

        val recipe = ShapelessRecipe(endBottleNamespace, itemStack()).apply {
            addIngredient(ItemStack(Material.POTION).apply {
                itemMeta = (itemMeta as PotionMeta).apply {
                    basePotionData = PotionData(PotionType.AWKWARD)
                }
            })
            addIngredient(Material.DRAGON_HEAD)
        }
    }
}