package me.crylonz;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.crylonz.utils.ConfigKey;
import org.bukkit.entity.Player;

import static me.crylonz.Utils.*;
import static me.crylonz.deadchest.DeadChest.dcConfig;
import static me.crylonz.deadchest.DeadChest.plugin;

public class WorldGuardSoftDependenciesChecker {

    public void load() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            BooleanFlag owner_flag = new BooleanFlag("dc-owner");
            registry.register(owner_flag);
            DEADCHEST_OWNER_FLAG = owner_flag;

            BooleanFlag nobody_flag = new BooleanFlag("dc-guest");
            registry.register(nobody_flag);
            DEADCHEST_GUEST_FLAG = nobody_flag;

            BooleanFlag member_flag = new BooleanFlag("dc-member");
            registry.register(member_flag);
            DEADCHEST_MEMBER_FLAG = member_flag;

        } catch (
                FlagConflictException e) {

            plugin.getLogger().warning("Conflict in DeadChest flags");
        }
    }

    public boolean worldGuardChecker(Player p) {

        if (!dcConfig.getBoolean(ConfigKey.WORLD_GUARD_DETECTION)) {
            return true;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regions = container.get(BukkitAdapter.adapt(p.getLocation().getWorld()));

            if (regions != null) {
                BlockVector3 position = BlockVector3.at(p.getLocation().getX(),
                        p.getLocation().getY(), p.getLocation().getZ());
                ApplicableRegionSet set = regions.getApplicableRegions(position);

                if (set.size() != 0) {

                    // retrieve the highest priority
                    ProtectedRegion pr = set.getRegions().iterator().next();
                    for (ProtectedRegion pRegion : set.getRegions()) {
                        if (pRegion.getPriority() > pr.getPriority()) {
                            pr = pRegion;
                        }
                    }

                    Boolean ownerFlag = pr.getFlag(DEADCHEST_OWNER_FLAG);
                    Boolean memberFlag = pr.getFlag(DEADCHEST_MEMBER_FLAG);
                    Boolean guestFlag = pr.getFlag(DEADCHEST_GUEST_FLAG);
                    if (ownerFlag != null && ownerFlag) {
                        if (pr.getOwners().contains(p.getUniqueId()) || p.isOp()) {
                            return true;
                        }
                    } else if (memberFlag != null && memberFlag) {
                        if (pr.getMembers().contains(p.getUniqueId()) || p.isOp()) {
                            return true;
                        }
                    } else if (guestFlag != null && guestFlag) {
                        return true;
                    } else {
                        return p.isOp();
                    }

                    generateLog("Player [" + p.getName() + "] died without [ Worldguard] region permission : No Deadchest generated");
                    return false;
                }
            }
            return true;
        } catch (NoClassDefFoundError e) {
            return true;
        }
    }

}
