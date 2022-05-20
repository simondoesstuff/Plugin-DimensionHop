package tech.simonwalker.dimensionhop

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.World.Environment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import tech.simonwalker.dimensionhop.LocationUtils.DimensionCordConvertUtil
import tech.simonwalker.dimensionhop.LocationUtils.SafeLocationUtil
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

class DrinkEvent : Listener {
    private val closures = mutableMapOf<UUID, Channel<PlayerEvent>>()

    private suspend fun eventConsumer(player: Player, channel: Channel<PlayerEvent>) {
        val drinkEvent = waitFor<PlayerItemConsumeEvent>(channel)

        var targetDimension: Environment
        var refundItem: ItemStack

        // here we initialize targetDimension and refundItem.
        // if the player is in an invalid dimension (endBottle, but in nether), we
        // also cancel the coroutine.
        drinkEvent.item.itemMeta.persistentDataContainer.let {
            var value: Environment  // Not a dimensional bottle
            val localDimension = player.world.environment

            if (it.has(CustomItemBuilder.netherBottleNamespace)) {
                value = Environment.NETHER
                refundItem = ItemStack(Material.CRYING_OBSIDIAN)
            } else if (it.has(CustomItemBuilder.endBottleNamespace)) {
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
        val nauseaTimerJob = DimensionHopPlugin.runnable {
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

        try {
            SafeLocationUtil.adjustToSafeLocation(translatedLocation).let {
                runSync { player.playEffect(EntityEffect.TELEPORT_ENDER) }
                delay(500)
                runSync { player.teleport(it) }
            }
        } catch (e: Exception) {
            player.sendMessage(Component.text("You could not be teleported because it would be fatal.").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.ITALIC))
            runSync {
                player.playEffect(EntityEffect.VILLAGER_ANGRY)
                player.inventory.addItem(refundItem)
            }
        }

        nauseaTimerJob.cancel()
    }

    private fun runSync(r: () -> Unit) = DimensionHopPlugin.runnable(r).runTask()

    private suspend inline fun <reified T : PlayerEvent> waitFor(channel: Channel<PlayerEvent>): T {
        while (true) {
            channel.receive().also {
                if (it is T) return it
            }
        }
    }

    private fun processEvents(e: PlayerEvent) {
        val uuid = e.player.uniqueId
        val closure = closures[uuid]

        // here we are starting a new coroutine
        if (closure == null) {
            // the channel has to have an unlimited buffer so that no threads
            // can be blocked waiting for the channel to empty
            closures[uuid] = Channel<PlayerEvent>(Channel.UNLIMITED).also {
                CoroutineScope(EmptyCoroutineContext).launch {
                    // here we start the coroutine as a background task
                    // and remove the channel from the map when it completes
                    it.send(e)
                    eventConsumer(e.player, it)
                    closures.remove(uuid)
                }
            }
        } else {
            // coroutine already exists, just feed it
            closure.let { runBlocking { it.send(e) } }
        }
    }

    @EventHandler
    fun onDrink(event: PlayerItemConsumeEvent) = processEvents(event)

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (event.from.toVector() == event.to.toVector()) return    // ignore if it was only a head movement
        processEvents(event)
    }
}