package tech.simonwalker.dimensionhop

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import kotlin.math.round
import kotlin.math.roundToInt


/**
 * Nearly all of the following code is copied from
 * EssentialX / Essentials. It has been refactored into
 * kotlin and cleaned up.
 */
object SafeLocationUtil {
    private const val RADIUS = 3

    // Initializing VOLUME
    // Create a list of all the vectors in a cube within a radius
    private val VOLUME: MutableList<Vector> = mutableListOf<Vector>().apply {
        for (x in -RADIUS..RADIUS)
            for (y in -RADIUS..RADIUS)
                for (z in -RADIUS..RADIUS)
                    add(Vector(x, y, z))

        sortWith(Comparator.comparingInt { a -> (a.x * a.x + a.y * a.y + a.z * a.z).toInt() })
    }

    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    fun isSafeLocation(location: Location): Boolean {
        val feet = location.block

        // check feet
        if (!feet.type.isOccluding && !feet.location.add(0.0, 1.0, 0.0).block.type.isOccluding) {
            return false // not transparent (will suffocate)
        }

        // check head
        feet.getRelative(BlockFace.UP).also {
            if (!it.type.isOccluding) {
                return false // not transparent (will suffocate)
            }
        }

        // check ground
        feet.getRelative(BlockFace.DOWN).also {
            return it.type.isSolid
        }
    }

    /**
     * Adjust a location to be inside the world border.
     */
    fun adjustToWorldBorder(loc: Location): Location {
        val world = loc.world
        val center = world.worldBorder.center

        fun cordInBorder(i: Int, centerAxis: Int): Int {
            val radius = world.worldBorder.size.toInt() / 2
            val i1 = centerAxis - radius
            val i2 = centerAxis + radius

            if (i < i1) {
                return i1
            } else if (i > i2) {
                return i2
            }

            return i
        }

        return Location(
            world,
            cordInBorder(loc.blockX, center.blockX).toDouble(),
            loc.blockY.toDouble(),
            cordInBorder(loc.blockZ, center.blockZ).toDouble()
        )
    }

    /**
     * Adjusts a given location to be safe to teleport to.
     *
     * @param loc The location to adjust
     */
    fun getSafeDestination(loc: Location): Location {
        if (loc.world == null) {
            throw Exception("World must not be null")
        }

        val world: World = loc.world
        val worldMinY: Int = world.minHeight
        val worldLogicalY: Int = world.logicalHeight
        val worldMaxY = if (loc.blockY < worldLogicalY) worldLogicalY else world.maxHeight

        var x: Int = loc.blockX
        var y = loc.y.roundToInt()
        var z: Int = loc.blockZ

        with(adjustToWorldBorder(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
            x = blockX
            z = blockZ
        }

        val origX: Int = x
        val origY: Int = y
        val origZ: Int = z

        while (!isSafeLocation(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
            y -= 1
            if (y < 0) {
                y = origY
                break
            }
        }

        if (!isSafeLocation(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
            x = if (round(loc.x).toInt() == origX) x - 1 else x + 1
            z = if (round(loc.z).toInt() == origZ) z - 1 else z + 1
        }

        var i = 0

        while (!isSafeLocation(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
            i++

            if (i >= VOLUME.size) {
                x = origX
                y = (origY + RADIUS).coerceIn(worldMinY, worldMaxY)
                z = origZ
                break
            }

            x = (origX + VOLUME[i].x).toInt()
            y = (origY + VOLUME[i].y.toInt()).coerceIn(worldMinY, worldMaxY)
            z = (origZ + VOLUME[i].z).toInt()
        }

        while (!isSafeLocation(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
            y += 1
            if (y >= worldMaxY) {
                x += 1
                break
            }
        }

        while (!isSafeLocation(Location(world, x.toDouble(), y.toDouble(), z.toDouble()))) {
            y -= 1
            if (y <= worldMinY + 1) {
                x += 1
                // Allow spawning at the top of the world, but not above the nether roof
                y = (world.getHighestBlockYAt(x, z) + 1).coerceAtMost(worldMaxY)
                if (x - 48 > loc.blockX) {
                    throw Exception("No safe location found.")
                }
            }
        }

        return Location(world, x + 0.5, y.toDouble(), z + 0.5, loc.yaw, loc.pitch)
    }
}