package tech.simonwalker.dimensionhop

import org.bukkit.plugin.java.JavaPlugin

class PortalBottlesPlugin : JavaPlugin() {
    companion object {
        lateinit var inst: PortalBottlesPlugin
            private set

        // A static call to make BukkitRunnables easier to work with.
        // Automatically registers the plugin instance to the runnable.
        fun runnable(task: () -> Unit) = object: PluginSpecificRunnable(inst) {
            override fun run() = task()
        }
    }

    override fun onEnable() {
        // Plugin startup logic
        logger.info("Everything is in working order! :)")
        inst = this

        // Register events
        server.pluginManager.registerEvents(DrinkEvent(), this)

        registerCustomRecipes()
    }

    private fun registerCustomRecipes() {
        server.addRecipe(CustomItemBuilder.NetherBottle.recipe)
        server.addRecipe(CustomItemBuilder.EndBottle.recipe)
    }
}