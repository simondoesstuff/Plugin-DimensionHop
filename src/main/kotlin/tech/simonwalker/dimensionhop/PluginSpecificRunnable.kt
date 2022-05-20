package tech.simonwalker.dimensionhop

import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

/**
 * A bukkit runnable that remembers a specific plugin
 * for later reference in the task methods.
 */
abstract class PluginSpecificRunnable(private val plugin: Plugin) : BukkitRunnable() {
    fun runTask(): BukkitTask {
        return super.runTask(plugin)
    }

    fun runTaskAsynchronously(): BukkitTask {
        return super.runTaskAsynchronously(plugin)
    }

    fun runTaskLater(delay: Long): BukkitTask {
        return super.runTaskLater(plugin, delay)
    }

    fun runTaskLaterAsynchronously(delay: Long): BukkitTask {
        return super.runTaskLaterAsynchronously(plugin, delay)
    }

    fun runTaskTimer(delay: Long, period: Long): BukkitTask {
        return super.runTaskTimer(plugin, delay, period)
    }

    fun runTaskTimerAsynchronously(delay: Long, period: Long): BukkitTask {
        return super.runTaskTimerAsynchronously(plugin, delay, period)
    }
}