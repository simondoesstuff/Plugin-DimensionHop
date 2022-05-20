package tech.simonwalker.dimensionhop.LocationUtils

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.World.Environment
import org.bukkit.util.Vector


object DimensionCordConvertUtil {
    val scaleFactor = mapOf(
        Environment.CUSTOM to 1.0,
        Environment.NORMAL to 1.0,
        Environment.NETHER to 1.0 / 8.0,
        Environment.THE_END to 1.0 / 16.0,
    )

    fun convert(loc: Location, targetEnvironment: Environment): Location {
        Bukkit.getWorlds().forEach {
            if (it.environment == targetEnvironment) {
                return convert(loc, it)
            }
        }

        throw Exception("No world found with environment $targetEnvironment")
    }

    fun convert(loc: Location, targetWorld: World): Location {
        val originEnvironment = loc.world.environment
        val targetEnvironment = targetWorld.environment
        val originScale = scaleFactor[originEnvironment] ?: 1.0
        val targetScale = scaleFactor[targetEnvironment] ?: 1.0

        val relativeFactor = targetScale / originScale

        return Location(
            targetWorld,
            loc.blockX * relativeFactor + .5,
            loc.blockY.toDouble(),
            loc.blockZ * relativeFactor + .5
        )
    }
}