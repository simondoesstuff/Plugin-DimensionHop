package tech.simonwalker.dimensionhop.LocationUtils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector


object SafeLocationUtil {
    val damagingBlocks = arrayOf(
        Material.CACTUS,
        Material.CAMPFIRE,
        Material.FIRE,
        Material.MAGMA_BLOCK,
        Material.SOUL_CAMPFIRE,
        Material.SOUL_FIRE,
        Material.SWEET_BERRY_BUSH,
        Material.WITHER_ROSE,
        Material.LAVA
    )

    // Create a list of all the vectors in a cube within a radius
    private val searchCube: MutableList<Vector> = mutableListOf<Vector>().apply {
        val radiusHori = 5
        val radiusVerti = 10

        for (y in -radiusVerti..radiusVerti)
            for (x in -radiusHori..radiusHori)
                for (z in -radiusHori..radiusHori)
                    add(Vector(x, y, z))

        sortedBy { it.x + it.y + it.z }
    }

    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    fun isLocationSafe(location: Location): Boolean {
        fun Material.isDamaging(): Boolean = this in damagingBlocks

        val feet = location.block

        // check feet and head
        arrayOf(feet, feet.getRelative(BlockFace.UP)).forEach {
            if (it.type.isOccluding) return false
            if (it.type.isDamaging()) return false
        }

        // check ground
        feet.getRelative(BlockFace.DOWN).also {
            if (!it.type.isSolid) return false
            if (it.type.isDamaging()) return false
        }

        return true
    }

    /**
     * Adjust a location to be inside the world border.
     */
    fun adjustToWorldBorder(loc: Location): Location {
        val world = loc.world
        val center = world.worldBorder.center

        fun axisInRange(i: Double, center: Double): Double {
            val radius = world.worldBorder.size.toInt() / 2
            val i1 = center - radius
            val i2 = center + radius

            if (i < i1) {
                return i1
            } else if (i > i2) {
                return i2
            }

            return i
        }

        return Location(world, axisInRange(loc.x, center.x), loc.y, axisInRange(loc.z, center.z))
    }

    /**
     * Adjusts a given location to be safe to teleport to.
     *
     * @param loc The location to adjust
     */
    fun adjustToSafeLocation(loc: Location): Location? {
        adjustToWorldBorder(loc).also {
            if (isLocationSafe(it)) {
                return it
            }
        }

        // in order to find a safe location, we will
        // iterate through all the vectors in a cube
        // and check if they are safe
        for (vector in searchCube) {
            loc.clone().add(vector).also {
                if (isLocationSafe(it)) {
                    return it
                }
            }
        }

        return null
    }
}