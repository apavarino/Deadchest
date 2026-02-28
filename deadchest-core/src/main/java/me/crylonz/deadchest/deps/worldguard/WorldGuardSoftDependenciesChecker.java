package me.crylonz.deadchest.deps.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.crylonz.deadchest.DeadChestLoader;
import me.crylonz.deadchest.utils.ConfigKey;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.crylonz.deadchest.DeadChestLoader.config;
import static me.crylonz.deadchest.utils.Utils.generateLog;

public class WorldGuardSoftDependenciesChecker {

    public static StateFlag DEADCHEST_GUEST_FLAG;
    public static StateFlag DEADCHEST_OWNER_FLAG;
    public static StateFlag DEADCHEST_MEMBER_FLAG;

    public void load() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag owner_flag = new StateFlag("dc-owner", false);
            registry.register(owner_flag);
            DEADCHEST_OWNER_FLAG = owner_flag;

            StateFlag nobody_flag = new StateFlag("dc-guest", false);
            registry.register(nobody_flag);
            DEADCHEST_GUEST_FLAG = nobody_flag;

            StateFlag member_flag = new StateFlag("dc-member", false);
            registry.register(member_flag);
            DEADCHEST_MEMBER_FLAG = member_flag;

        } catch (FlagConflictException e) {
            DeadChestLoader.log.warning("Conflict in Deadchest flags");
        }
    }

    public boolean worldGuardChecker(Player p) {

        if (!config.getBoolean(ConfigKey.WORLD_GUARD_DETECTION)) {
            return true;
        }

        try {
/*            if (p.isOp()) {
                return true;
            }*/
            final RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            final ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(p.getLocation()));
            final LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
            final UUID uuid = p.getUniqueId();
            boolean defaultAllow = config.getBoolean(ConfigKey.WORLD_GUARD_FLAG_DEFAULT);
            final Set<Boolean> defaults = new HashSet<>();

            for (ProtectedRegion region : set.getRegions()) {
                State stateOwner = checkRegionFlag(region, DEADCHEST_OWNER_FLAG, region.getOwners(), uuid);
                if (stateOwner == State.ALLOWED) return true;
                State stateMember = checkRegionFlag(region, DEADCHEST_MEMBER_FLAG, region.getMembers(), uuid);
                if (stateMember == State.ALLOWED) return true;

                if (stateMember == State.NONE && stateOwner == State.NONE) {
                    defaults.add(true);
                } else {
                    if (stateOwner == State.DENY || stateMember == State.DENY)
                        return false;
                    defaults.add(false);
                }
            }
            if (defaults.size() == 1 && defaults.contains(true))
                return defaultAllow;

            final StateFlag.State state = set.queryState(localPlayer, DEADCHEST_GUEST_FLAG);
            System.out.println("DEADCHEST_GUEST_FLAG " + state);
            if (state == StateFlag.State.ALLOW) return true;
            if (state == StateFlag.State.DENY) {
                generateLog("Player [" + p.getName() + "] died without [WorldGuard] permission: No Deadchest generated");
                return false;
            }
            return defaultAllow;
        } catch (NoClassDefFoundError e) {
            return true;
        }
    }

    private State checkRegionFlag(ProtectedRegion region, StateFlag flag, DefaultDomain uuids, UUID playerUUID) {
        StateFlag.State state = region.getFlag(flag);
        if (state == null) return State.NONE;
        if (state == StateFlag.State.DENY) return State.DENY;
        return state == StateFlag.State.ALLOW && uuids.contains(playerUUID) ? State.ALLOWED : State.NOT_APPLICABLE;
    }

    private enum State {
        NONE,
        DENY,
        ALLOWED,
        NOT_APPLICABLE
    }
}
