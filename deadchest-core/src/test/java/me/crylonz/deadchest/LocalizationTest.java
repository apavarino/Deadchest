package me.crylonz.deadchest;

import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LocalizationTest {

    @Test
    void formatAndPrefixedApplyPlaceholders() {
        Localization localization = new Localization();
        Map<String, Object> values = new HashMap<>();
        values.put("common.prefix", "[DeadChest] ");
        values.put("commands.error.bad-args", "Bad argument(s) for /dc {0}");
        localization.set(values);

        String formatted = localization.format("commands.error.bad-args", "remove");
        String prefixed = localization.prefixed("commands.error.bad-args", "remove");

        assertTrue(formatted.contains("remove"));
        assertTrue(prefixed.startsWith("[DeadChest] "));
        assertTrue(prefixed.contains("remove"));
    }

    @Test
    void reloadLanguageCreatesFilesAndLoadsSelectedLanguage(@TempDir Path tempDir) throws Exception {
        Plugin plugin = mockPluginWithResources(tempDir, Map.of(
                "localization/en.json", minimalJson("Reload EN", "[EN] "),
                "localization/fr.json", minimalJson("Recharge FR", "[FR] ")
        ));

        Localization localization = new Localization(plugin);
        localization.reloadLanguage("fr");

        assertEquals("Recharge FR", localization.get("commands.reload.success"));
        assertEquals("[FR] Recharge FR", localization.prefixed("commands.reload.success"));

        assertTrue(Files.exists(tempDir.resolve("localization/en.json")));
        assertTrue(Files.exists(tempDir.resolve("localization/fr.json")));
    }

    @Test
    void reloadLanguageSanitizesCurrentJsonWithoutOverridingExistingValues(@TempDir Path tempDir) throws Exception {
        Plugin plugin = mockPluginWithResources(tempDir, Map.of(
                "localization/en.json", minimalJson("Reload Default", "[Default] "),
                "localization/fr.json", minimalJson("Reload FR", "[FR] ")
        ));

        Path localizationDir = tempDir.resolve("localization");
        Files.createDirectories(localizationDir);
        Files.writeString(localizationDir.resolve("en.json"), "{\n" +
                "  \"common.prefix\": \"[CUSTOM] \",\n" +
                "  \"obsolete.key\": \"to-remove\"\n" +
                "}\n", StandardCharsets.UTF_8);

        Localization localization = new Localization(plugin);
        localization.reloadLanguage("en");

        JSONObject json = readJson(localizationDir.resolve("en.json"));
        assertEquals("[CUSTOM] ", json.get("common.prefix"));
        assertEquals("Reload Default", json.get("commands.reload.success"));
        assertNull(json.get("obsolete.key"));
    }

    @Test
    void reloadLanguageMigratesLegacyLocaleYamlIntoEnJson(@TempDir Path tempDir) throws Exception {
        Plugin plugin = mockPluginWithResources(tempDir, Map.of(
                "localization/en.json", minimalJson("Reload Default", "[Default] "),
                "localization/fr.json", minimalJson("Reload FR", "[FR] ")
        ));

        Files.writeString(tempDir.resolve("locale.yml"), "localisation:\n" +
                "  loc_prefix: \"[LEGACY] \"\n" +
                "  loc_reload: \"Reload Legacy\"\n", StandardCharsets.UTF_8);

        Localization localization = new Localization(plugin);
        localization.reloadLanguage("en");

        JSONObject json = readJson(tempDir.resolve("localization/en.json"));
        assertEquals("[LEGACY] ", json.get("common.prefix"));
        assertEquals("Reload Legacy", json.get("commands.reload.success"));
        assertTrue(Files.exists(tempDir.resolve("locale.legacy.yml")));
        assertFalse(Files.exists(tempDir.resolve("locale.yml")));
    }

    @Test
    void reloadLanguageMigratesLegacyPercentPlaceholdersToIndexedFormat(@TempDir Path tempDir) throws Exception {
        Plugin plugin = mockPluginWithResources(tempDir, Map.of(
                "localization/en.json", "{\n" +
                        "  \"common.prefix\": \"[Default] \",\n" +
                        "  \"commands.reload.success\": \"Reload Default\",\n" +
                        "  \"hologram.timer\": \"{0}h {1}m {2}s left\"\n" +
                        "}\n",
                "localization/fr.json", minimalJson("Reload FR", "[FR] ")
        ));

        Files.writeString(tempDir.resolve("locale.yml"), "localisation:\n" +
                "  holo_timer: \"%hours%h %minutes%m %seconds%s left\"\n", StandardCharsets.UTF_8);

        Localization localization = new Localization(plugin);
        localization.reloadLanguage("en");

        JSONObject json = readJson(tempDir.resolve("localization/en.json"));
        assertEquals("{0}h {1}m {2}s left", json.get("hologram.timer"));
    }

    private static Plugin mockPluginWithResources(Path dataFolder, Map<String, String> resources) {
        Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(dataFolder.toFile());
        when(plugin.getLogger()).thenReturn(Logger.getLogger("LocalizationTest"));

        when(plugin.getResource(anyString())).thenAnswer(invocation -> {
            String path = invocation.getArgument(0, String.class);
            String content = resources.get(path);
            if (content == null) {
                return null;
            }
            return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        });

        doAnswer(invocation -> {
            String path = invocation.getArgument(0, String.class);
            boolean replace = invocation.getArgument(1, Boolean.class);

            String content = resources.get(path);
            if (content == null) {
                throw new IllegalArgumentException("Resource not found: " + path);
            }

            File target = dataFolder.resolve(path).toFile();
            if (target.exists() && !replace) {
                return null;
            }

            File parent = target.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            Files.writeString(target.toPath(), content, StandardCharsets.UTF_8);
            return null;
        }).when(plugin).saveResource(anyString(), anyBoolean());

        return plugin;
    }

    private static String minimalJson(String reloadMessage, String prefix) {
        return "{\n" +
                "  \"common.prefix\": \"" + prefix + "\",\n" +
                "  \"commands.reload.success\": \"" + reloadMessage + "\"\n" +
                "}\n";
    }

    private static JSONObject readJson(Path path) throws Exception {
        String raw = Files.readString(path, StandardCharsets.UTF_8);
        return (JSONObject) new JSONParser().parse(raw);
    }
}
