package me.crylonz.utils;


import org.bukkit.entity.Player;

import java.util.Locale;

import static me.crylonz.DeadChest.config;

public final class ExpUtils {

    public static void setExp(Player target, String stringAmount) throws NumberFormatException {
        String lowerCase = stringAmount.toLowerCase(Locale.ENGLISH);
        long amount;
        if (stringAmount.contains("l")) {
            int neededLevel = Integer.parseInt(lowerCase.replaceAll("l", "")) + target.getLevel();
            amount = (long) (getExpToLevel(neededLevel) + (getTotalExperience(target) - getExpToLevel(target.getLevel())));
            setTotalExperience(target, 0);
        } else {
            amount = Long.parseLong(lowerCase);
        }

        amount += (long) getTotalExperience(target);
        if (amount > 2147483647L) {
            amount = 2147483647L;
        }

        if (amount < 0L) {
            amount = 0L;
        }

        setTotalExperience(target, (int) amount);
    }

    public static void setTotalExperience(Player player, int exp) throws IllegalArgumentException {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        } else {
            player.setExp(0.0F);
            player.setLevel(0);
            player.setTotalExperience(0);
            int amount = exp;

            while (amount > 0) {
                int expToLevel = getExpAtLevel(player);
                amount -= expToLevel;
                if (amount >= 0) {
                    player.giveExp(expToLevel);
                } else {
                    amount += expToLevel;
                    player.giveExp(amount);
                    amount = 0;
                }
            }

        }
    }

    private static int getExpAtLevel(Player player) {
        return getExpAtLevel(player.getLevel());
    }

    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else {
            return level <= 30 ? 5 * level - 38 : 9 * level - 158;
        }
    }

    public static int getExpToLevel(int level) {
        int currentLevel = 0;

        int exp;
        for (exp = 0; currentLevel < level; ++currentLevel) {
            exp += getExpAtLevel(currentLevel);
        }

        if (exp < 0) {
            exp = 2147483647;
        }

        return exp;
    }

    public static int getTotalExperience(Player player) {
        int exp = Math.round((float) getExpAtLevel(player) * player.getExp());

        for (int currentLevel = player.getLevel(); currentLevel > 0; exp += getExpAtLevel(currentLevel)) {
            --currentLevel;
        }

        if (exp < 0) {
            exp = 2147483647;
        }

        return exp;
    }

    public static int getTotalExperienceToStore(Player player) {
        if (config.getBoolean(ConfigKey.STORE_XP)) {
            return getTotalExperience(player) * config.getInt(ConfigKey.STORE_XP_PERCENTAGE) / 100;
        }
        return 0;
    }
}