package me.crylonz.deadchest.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum EffectAnimationStyle {
    SOUL("soul",
            new String[]{"SOUL", "SOUL_FIRE_FLAME", "SPELL_MOB_AMBIENT"}),
    FLAME("flame",
            new String[]{"SMALL_FLAME", "FLAME"}),
    ENDER("ender",
            new String[]{"PORTAL", "DRAGON_BREATH", "SPELL_WITCH"});

    private final String id;
    private final String[] particleCandidates;

    EffectAnimationStyle(String id, String[] particleCandidates) {
        this.id = id;
        this.particleCandidates = particleCandidates;
    }

    public String id() {
        return id;
    }

    public String[] particleCandidates() {
        return Arrays.copyOf(particleCandidates, particleCandidates.length);
    }

    public static EffectAnimationStyle fromInput(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        final String normalized = raw.trim()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-')
                .replace(' ', '-');

        for (EffectAnimationStyle style : values()) {
            if (style.id.equals(normalized) || style.name().equalsIgnoreCase(normalized)) {
                return style;
            }
        }
        return null;
    }

    public static String listIds() {
        return Arrays.stream(values())
                .map(EffectAnimationStyle::id)
                .collect(Collectors.joining(", "));
    }
}
