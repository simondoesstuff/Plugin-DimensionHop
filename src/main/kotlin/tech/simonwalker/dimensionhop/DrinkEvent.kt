package tech.simonwalker.dimensionhop

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.World.Environment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tech.simonwalker.dimensionhop.LocationUtils.DimensionCordConvertUtil
import tech.simonwalker.dimensionhop.LocationUtils.SafeLocationUtil

class DrinkEvent : CoroutinePlayerEventListener() {
    override suspend fun eventConsumer(player: Player, channel: Channel<PlayerEvent>) {
        val drinkEvent = waitFor<PlayerItemConsumeEvent>(channel)

        var targetDimension: Environment
        var refundItem: ItemStack

        // here we initialize targetDimension and refundItem.
        // if the player is in an invalid dimension (endBottle, but in nether), we
        // also cancel the coroutine.
        drinkEvent.item.let {
            var value: Environment  // Not a dimensional bottle
            val localDimension = player.world.environment

            if (CustomItemBuilder.NetherBottle.matches(it)) {
                value = Environment.NETHER
                refundItem = ItemStack(Material.CRYING_OBSIDIAN)
            } else if (CustomItemBuilder.EndBottle.matches(it)) {
                value = Environment.THE_END
                refundItem = ItemStack(Material.DRAGON_HEAD)
            } else return

            // the player is in an invalid dimension
            if ((localDimension == Environment.THE_END && value == Environment.NETHER) || (localDimension == Environment.NETHER && value == Environment.THE_END)) {
                player.sendMessage(Component.text("You cannot drink from the bottle in this dimension.").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.ITALIC))
                runSync {
                    player.location.createExplosion(1.5f)
                    player.inventory.addItem(refundItem)
                }
                return
            }

            // Already in the target dimension
            if (localDimension == value) value = Environment.NORMAL
            targetDimension = value
        }

        // to give the player nausea, we have to add it every tick
        val nauseaTimerJob = PortalBottlesPlugin.runnable {
            player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 90, 1, false, true, false))
        }.runTaskTimer(0, 1)

        // wait 4.5 seconds for the player to move
        withTimeoutOrNull(4500) { waitFor<PlayerMoveEvent>(channel) }?.let {
            player.sendMessage(Component.text("You must stay in one place to cross dimensions.").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.ITALIC))
            player.inventory.addItem(refundItem)
            nauseaTimerJob.cancel()
            return
        }

        // continue teleporting
        val translatedLocation = DimensionCordConvertUtil.convert(player.location, targetDimension)
        player.sendMessage("attempting to teleport to ${translatedLocation}")

        // if the adjusted safe location isnt null
        SafeLocationUtil.adjustToSafeLocation(translatedLocation)?.let {
            player.sendMessage("Redirected to safe location: $it")
            nauseaTimerJob.cancel()
            delay(500)
            runSync {
                player.playEffect(EntityEffect.TELEPORT_ENDER)
                player.teleport(it)
            }
            return
        }

        // we failed to find a safe location
        player.sendMessage(Component.text("You could not be teleported because it would be fatal.").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.ITALIC))
        nauseaTimerJob.cancel()
        runSync {
            player.playEffect(EntityEffect.VILLAGER_ANGRY)
            player.inventory.addItem(refundItem)
        }
    }

    @EventHandler
    fun onDrink(event: PlayerItemConsumeEvent) = processEvents(event)

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        // ignore if it was only a head movement
        if (event.from.toVector() != event.to.toVector()) processEvents(event)
    }
}