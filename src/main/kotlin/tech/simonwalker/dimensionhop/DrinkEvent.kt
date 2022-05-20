package tech.simonwalker.dimensionhop

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.persistence.PersistentDataType

class DrinkEvent : Listener {
    @EventHandler
    fun onDrink(event: PlayerItemConsumeEvent) {
        val dataContainer = event.item.itemMeta.persistentDataContainer

        if (dataContainer.has(CustomItemBuilder.endBottleNamespace, PersistentDataType.BYTE)) {
            event.player.sendMessage("You drank the end bottle!")
        }
    }
}