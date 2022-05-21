package tech.simonwalker.dimensionhop

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEvent
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Uses coroutines from kotlinx to enable event handling
 * in a coroutine-friendly way. All events are passed to
 * running coroutines through channels and handled async.
 */
abstract class CoroutinePlayerEventListener : Listener {
    private val closures = mutableMapOf<UUID, Channel<PlayerEvent>>()

    protected abstract suspend fun eventConsumer(player: Player, channel: Channel<PlayerEvent>)

    protected fun runSync(r: () -> Unit) = PortalBottlesPlugin.runnable(r).runTask()

    protected suspend inline fun <reified T : PlayerEvent> waitFor(channel: Channel<PlayerEvent>): T {
        while (true) {
            channel.receive().also {
                if (it is T) return it
            }
        }
    }

    protected fun processEvents(e: PlayerEvent) {
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
}