package me.crylonz.deadchest

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.flags.BooleanFlag
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException
import me.crylonz.Utils
import me.crylonz.deadchest.DeadChest.Companion.dcConfig
import me.crylonz.deadchest.utils.ConfigKey.WORLD_GUARD_DETECTION
import org.bukkit.entity.Player

class WorldGuardSoftDependenciesChecker {
    fun load() {
        val registry = WorldGuard.getInstance().flagRegistry
        try {
            BooleanFlag("dc-owner").let {
                registry.register(it)
                Utils.DEADCHEST_OWNER_FLAG = it
            }

            BooleanFlag("dc-guest").let {
                registry.register(it)
                Utils.DEADCHEST_GUEST_FLAG = it
            }

            BooleanFlag("dc-member").let {
                registry.register(it)
                Utils.DEADCHEST_MEMBER_FLAG = it
            }

        } catch (e: FlagConflictException) {
            DeadChest.plugin.logger.warning("Conflict in DeadChest flags")
        }
    }

    fun worldGuardChecker(player: Player): Boolean {
        return if (!dcConfig.getBoolean(WORLD_GUARD_DETECTION)) {
            true
        } else try {
            val container = WorldGuard.getInstance().platform.regionContainer
            val regions = container[BukkitAdapter.adapt(player.location.world)]

            if (regions != null) {
                val position = BlockVector3.at(player.location.x, player.location.y, player.location.z)
                val set = regions.getApplicableRegions(position)
                if (set.size() != 0) {

                    // retrieve the highest priority
                    var protectedRegion = set.regions.iterator().next()

                    for (region in set.regions) {
                        if (region.priority > protectedRegion.priority) {
                            protectedRegion = region
                        }
                    }

                    val ownerFlag = protectedRegion.getFlag(Utils.DEADCHEST_OWNER_FLAG)
                    val memberFlag = protectedRegion.getFlag(Utils.DEADCHEST_MEMBER_FLAG)
                    val guestFlag = protectedRegion.getFlag(Utils.DEADCHEST_GUEST_FLAG)


                    if (ownerFlag != null && ownerFlag) {
                        if (protectedRegion.owners.contains(player.uniqueId) || player.isOp) {
                            return true
                        }
                    } else if (memberFlag != null && memberFlag) {
                        if (protectedRegion.members.contains(player.uniqueId) || player.isOp) {
                            return true
                        }
                    } else return if (guestFlag != null && guestFlag) {
                        true
                    } else {
                        player.isOp
                    }
                    Utils.generateLog("Player [" + player.name + "] died without [ Worldguard] region permission : No Deadchest generated")
                    return false
                }
            }
            true
        } catch (e: NoClassDefFoundError) {
            true
        }
    }
}

