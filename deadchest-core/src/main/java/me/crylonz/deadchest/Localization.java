package me.crylonz.deadchest;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Localization {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String LEGACY_FILE_NAME = "locale.yml";
    private static final String LOCALIZATION_DIRECTORY_NAME = "localization";
    private static final String JSON_EXTENSION = ".json";
    private static final String LOCALIZATION_RESOURCE_PREFIX = "localization/";
    private static final JSONParser JSON_PARSER = new JSONParser();
    private static final Pattern LEGACY_PLACEHOLDER_PATTERN = Pattern.compile("%([a-zA-Z0-9_]+)%");
    private static final List<String> BUNDLED_LANGUAGES = Arrays.asList(
            "en", "fr", "es", "de", "pt-br", "pl", "it", "zh-cn"
    );

    private static final Map<String, String> LEGACY_KEY_MAPPING = createLegacyKeyMapping();

    private final Plugin plugin;
    private Map<String, String> values = new HashMap<>();

    public Localization() {
        this(null);
    }

    public Localization(Plugin plugin) {
        this.plugin = plugin;
        this.values = plugin != null ? resolveEnglishDefaults() : new LinkedHashMap<>();
    }

    public String get(String key) {
        return values.getOrDefault(key, key);
    }

    public String format(String key, Object... args) {
        return applyPlaceholders(get(key), args);
    }

    public String prefixed(String key, Object... args) {
        return get("common.prefix") + format(key, args);
    }

    public Map<String, Object> getAll() {
        Map<String, Object> copy = new HashMap<>();
        values.forEach(copy::put);
        return copy;
    }

    public void set(Map<String, Object> local) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (local != null) {
            for (Map.Entry<String, Object> entry : local.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    normalized.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        }
        if (plugin == null) {
            this.values = normalized;
            return;
        }

        Map<String, String> englishDefaults = resolveEnglishDefaults();
        this.values = sanitizeLocalization(normalized, englishDefaults, englishDefaults);
    }

    public void reloadLanguage(String languageCode) {
        if (plugin == null) {
            this.values = new LinkedHashMap<>();
            return;
        }

        String normalizedLanguage = normalizeLanguage(languageCode);
        ensureLocalizationDirectoryExists();
        copyBundledDefaultsIfMissing();
        migrateLegacyLocaleYamlIntoDefaultLanguage();
        ensureLanguageFileExists(normalizedLanguage);
        Map<String, String> englishDefaults = resolveEnglishDefaults();
        sanitizeAllJsonLocalizationFiles(englishDefaults);

        Map<String, String> defaults = loadDefaultsForLanguage(normalizedLanguage, englishDefaults);
        Map<String, String> current = readJsonLocalization(getLocalizationFile(normalizedLanguage));
        Map<String, String> sanitized = sanitizeLocalization(current, defaults, englishDefaults);
        writeJsonLocalization(getLocalizationFile(normalizedLanguage), sanitized);
        this.values = sanitized;
    }

    private static String applyPlaceholders(String template, Object... args) {
        String out = template;
        if (args == null) {
            return out;
        }
        for (int i = 0; i < args.length; i++) {
            out = out.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return out;
    }

    private void ensureLocalizationDirectoryExists() {
        File dir = getLocalizationDirectory();
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void copyBundledDefaultsIfMissing() {
        for (String languageCode : BUNDLED_LANGUAGES) {
            copyBundledLanguageIfMissing(languageCode);
        }
    }

    private void copyBundledLanguageIfMissing(String languageCode) {
        File target = getLocalizationFile(languageCode);
        if (target.exists() || !hasBundledLanguage(languageCode)) {
            return;
        }

        try {
            plugin.saveResource(resourcePathForLanguage(languageCode), false);
        } catch (IllegalArgumentException ignored) {
            // Ignore if resource does not exist.
        }
    }

    private void ensureLanguageFileExists(String languageCode) {
        File selected = getLocalizationFile(languageCode);
        if (selected.exists()) {
            return;
        }

        if (hasBundledLanguage(languageCode)) {
            try {
                plugin.saveResource(resourcePathForLanguage(languageCode), false);
                return;
            } catch (IllegalArgumentException ignored) {
                // Continue with fallback copy.
            }
        }

        File defaultFile = getLocalizationFile(DEFAULT_LANGUAGE);
        if (defaultFile.exists()) {
            try {
                Files.copy(defaultFile.toPath(), selected.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                plugin.getLogger().warning("Failed to create localization file for '" + languageCode + "': " + ex.getMessage());
            }
        } else {
            throw new IllegalStateException("Missing bundled default localization resource: " + resourcePathForLanguage(DEFAULT_LANGUAGE));
        }
    }

    private void sanitizeAllJsonLocalizationFiles(Map<String, String> englishDefaults) {
        File localizationDir = getLocalizationDirectory();
        File[] files = localizationDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(JSON_EXTENSION));
        if (files == null) {
            return;
        }

        for (File file : files) {
            String fileName = file.getName();
            String language = fileName.substring(0, fileName.length() - JSON_EXTENSION.length());
            Map<String, String> defaults = loadDefaultsForLanguage(language, englishDefaults);
            Map<String, String> current = readJsonLocalization(file);
            Map<String, String> sanitized = sanitizeLocalization(current, defaults, englishDefaults);
            if (!sanitized.equals(current)) {
                writeJsonLocalization(file, sanitized);
            }
        }
    }

    private void migrateLegacyLocaleYamlIntoDefaultLanguage() {
        File legacyFile = new File(plugin.getDataFolder(), LEGACY_FILE_NAME);
        if (!legacyFile.exists()) {
            return;
        }

        YamlConfiguration legacyConfig = YamlConfiguration.loadConfiguration(legacyFile);
        ConfigurationSection section = legacyConfig.getConfigurationSection("localisation");
        Map<String, Object> legacyValues = section != null ? section.getValues(true) : legacyConfig.getValues(true);

        Map<String, String> englishDefaults = resolveEnglishDefaults();
        Map<String, String> defaults = loadDefaultsForLanguage(DEFAULT_LANGUAGE, englishDefaults);
        File defaultFile = getLocalizationFile(DEFAULT_LANGUAGE);
        Map<String, String> currentDefault = readJsonLocalization(defaultFile);
        Map<String, String> migrated = sanitizeLocalization(currentDefault, defaults, englishDefaults);

        for (Map.Entry<String, Object> entry : legacyValues.entrySet()) {
            String canonicalKey = toCanonicalFromLegacy(entry.getKey());
            if (englishDefaults.containsKey(canonicalKey) && entry.getValue() != null) {
                String migratedValue = migrateLegacyPlaceholders(String.valueOf(entry.getValue()));
                migrated.put(canonicalKey, migratedValue);
            }
        }

        writeJsonLocalization(defaultFile, migrated);

        File backup = new File(plugin.getDataFolder(), "locale.legacy.yml");
        try {
            Files.move(legacyFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.getLogger().info("Migrated legacy locale.yml to localization/en.json");
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not archive locale.yml after migration: " + ex.getMessage());
        }
    }

    private Map<String, String> loadDefaultsForLanguage(String languageCode, Map<String, String> englishDefaults) {
        Map<String, String> defaults = readBundledLocalization(languageCode);
        if (defaults.isEmpty()) {
            return new LinkedHashMap<>(englishDefaults);
        }
        return sanitizeLocalization(defaults, defaults, englishDefaults);
    }

    private Map<String, String> readBundledLocalization(String languageCode) {
        if (plugin == null) {
            return new LinkedHashMap<>();
        }

        String resourcePath = resourcePathForLanguage(languageCode);
        InputStream resourceStream = plugin.getResource(resourcePath);
        if (resourceStream == null) {
            return new LinkedHashMap<>();
        }

        try (InputStream input = resourceStream; Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            return parseJsonObject(reader);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to read bundled localization '" + resourcePath + "': " + ex.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private Map<String, String> readJsonLocalization(File file) {
        if (file == null || !file.exists()) {
            return new LinkedHashMap<>();
        }

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return parseJsonObject(reader);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to read localization file '" + file.getName() + "': " + ex.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private Map<String, String> parseJsonObject(Reader reader) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            Object parsed = JSON_PARSER.parse(reader);
            if (!(parsed instanceof JSONObject)) {
                return result;
            }

            JSONObject object = (JSONObject) parsed;
            for (Object key : object.keySet()) {
                Object value = object.get(key);
                if (key != null && value != null) {
                    result.put(String.valueOf(key), String.valueOf(value));
                }
            }
        } catch (IOException | ParseException ex) {
            if (plugin != null) {
                plugin.getLogger().warning("Invalid localization JSON: " + ex.getMessage());
            }
        }
        return result;
    }

    private void writeJsonLocalization(File file, Map<String, String> data) {
        if (file == null) {
            return;
        }

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (Writer writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
            writer.write(toPrettyJson(data));
        } catch (IOException ex) {
            if (plugin != null) {
                plugin.getLogger().warning("Could not write localization file '" + file.getName() + "': " + ex.getMessage());
            }
        }
    }

    private Map<String, String> sanitizeLocalization(Map<String, String> current,
                                                     Map<String, String> defaultsForLanguage,
                                                     Map<String, String> defaultsEnglish) {
        Map<String, String> sanitized = new LinkedHashMap<>();
        for (String key : defaultsEnglish.keySet()) {
            if (current.containsKey(key)) {
                sanitized.put(key, current.get(key));
            } else if (defaultsForLanguage.containsKey(key)) {
                sanitized.put(key, defaultsForLanguage.get(key));
            } else {
                sanitized.put(key, defaultsEnglish.get(key));
            }
        }
        return sanitized;
    }

    private boolean hasBundledLanguage(String languageCode) {
        if (plugin == null) {
            return false;
        }
        InputStream inputStream = plugin.getResource(resourcePathForLanguage(languageCode));
        if (inputStream == null) {
            return false;
        }
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
        return true;
    }

    private Map<String, String> resolveEnglishDefaults() {
        Map<String, String> fromBundled = readBundledLocalization(DEFAULT_LANGUAGE);
        if (!fromBundled.isEmpty()) {
            return fromBundled;
        }

        throw new IllegalStateException("Missing or invalid bundled localization resource: " + resourcePathForLanguage(DEFAULT_LANGUAGE));
    }

    private File getLocalizationDirectory() {
        return new File(plugin.getDataFolder(), LOCALIZATION_DIRECTORY_NAME);
    }

    private File getLocalizationFile(String languageCode) {
        return new File(getLocalizationDirectory(), normalizeLanguage(languageCode) + JSON_EXTENSION);
    }

    private static String normalizeLanguage(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return DEFAULT_LANGUAGE;
        }
        return languageCode.trim().toLowerCase(Locale.ROOT);
    }

    private static String toCanonicalFromLegacy(String key) {
        if (key == null) {
            return "";
        }
        return LEGACY_KEY_MAPPING.getOrDefault(key, key);
    }

    private static String migrateLegacyPlaceholders(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher matcher = LEGACY_PLACEHOLDER_PATTERN.matcher(input);
        Map<String, Integer> orderedPlaceholders = new LinkedHashMap<>();
        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            String token = matcher.group(1).toLowerCase(Locale.ROOT);
            Integer index = orderedPlaceholders.get(token);
            if (index == null) {
                index = orderedPlaceholders.size();
                orderedPlaceholders.put(token, index);
            }
            matcher.appendReplacement(output, Matcher.quoteReplacement("{" + index + "}"));
        }
        matcher.appendTail(output);
        return output.toString();
    }

    private static String resourcePathForLanguage(String languageCode) {
        return LOCALIZATION_RESOURCE_PREFIX + normalizeLanguage(languageCode) + JSON_EXTENSION;
    }

    private static String toPrettyJson(Map<String, String> data) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        int index = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            json.append("  \"").append(escapeJson(entry.getKey())).append("\": ")
                    .append("\"").append(escapeJson(entry.getValue())).append("\"");
            if (index < data.size() - 1) {
                json.append(",");
            }
            json.append("\n");
            index++;
        }
        json.append("}\n");
        return json.toString();
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (ch < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) ch));
                    } else {
                        escaped.append(ch);
                    }
            }
        }
        return escaped.toString();
    }

    private static Map<String, String> createLegacyKeyMapping() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("loc_prefix", "common.prefix");
        map.put("holo_owner", "hologram.owner");
        map.put("holo_timer", "hologram.timer");
        map.put("holo_loading", "hologram.loading");
        map.put("loc_not_owner", "chest.not-owner");
        map.put("loc_infinityChest", "chest.infinity");
        map.put("loc_endtimer", "chest.time-left");
        map.put("loc_reload", "commands.reload.success");
        map.put("loc_noperm", "commands.error.no-permission");
        map.put("loc_nodc", "commands.list.none.player");
        map.put("loc_nodcs", "commands.list.none.global");
        map.put("loc_dclistall", "commands.list.title.all");
        map.put("loc_dclistown", "commands.list.title.own");
        map.put("loc_doubleDC", "chest.double-block");
        map.put("loc_maxHeight", "death.above-max-height");
        map.put("loc_noDCG", "death.not-generated");
        map.put("loc_givebackInfo", "commands.giveback.target-not-found");
        map.put("loc_dcgbsuccess", "commands.giveback.success.sender");
        map.put("loc_gbplayer", "commands.giveback.success.target");
        map.put("loc_chestPos", "death.position");
        map.put("loc_noPermsToGet", "chest.no-permission-open");
        return map;
    }
}
