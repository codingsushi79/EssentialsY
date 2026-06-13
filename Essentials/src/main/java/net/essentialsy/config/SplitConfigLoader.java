package net.essentialsy.config;

import com.earth2me.essentials.Essentials;
import net.essentialsy.module.ModuleType;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads split configuration files from the config/ directory and merges them
 * into a single virtual configuration node for backward-compatible access.
 */
public class SplitConfigLoader {
    private static final String[] CORE_CONFIG_FILES = {"core.yml", "homes.yml", "economy.yml", "help.yml", "chat.yml", "protect.yml"};

    private static final Map<String, ModuleType> MODULE_CONFIG_FILES;

    static {
        final Map<String, ModuleType> moduleFiles = new LinkedHashMap<>();
        moduleFiles.put("antibuild.yml", ModuleType.ANTIBUILD);
        moduleFiles.put("spawn.yml", ModuleType.SPAWN);
        MODULE_CONFIG_FILES = Collections.unmodifiableMap(moduleFiles);
    }

    private final Essentials ess;
    private final ModulesConfig modulesConfig;
    private final File configDir;
    private final File legacyConfig;
    private CommentedConfigurationNode mergedNode;

    public SplitConfigLoader(final Essentials ess, final ModulesConfig modulesConfig) {
        this.ess = ess;
        this.modulesConfig = modulesConfig;
        this.configDir = new File(ess.getDataFolder(), "config");
        this.legacyConfig = new File(ess.getDataFolder(), "config.yml");
    }

    public void load() {
        if (!configDir.exists() && legacyConfig.exists()) {
            migrateLegacyConfig();
        }

        if (!configDir.exists()) {
            initializeSplitConfigs();
        }

        mergedNode = YamlConfigurationLoader.builder().build().createNode();
        for (final String fileName : CORE_CONFIG_FILES) {
            mergeConfigFile(fileName);
        }
        for (final Map.Entry<String, ModuleType> entry : MODULE_CONFIG_FILES.entrySet()) {
            if (modulesConfig != null && !modulesConfig.isEnabled(entry.getValue())) {
                continue;
            }
            mergeConfigFile(entry.getKey());
        }
    }

    private void mergeConfigFile(final String fileName) {
        final File file = new File(configDir, fileName);
        if (!file.exists()) {
            copyTemplate(fileName, file);
        }
        try {
            final CommentedConfigurationNode fileNode = YamlConfigurationLoader.builder()
                    .file(file)
                    .build()
                    .load();
            mergeNode(mergedNode, fileNode);
        } catch (final ConfigurateException e) {
            ess.getLogger().log(Level.SEVERE, "Failed to load config file: " + fileName, e);
        }
    }

    public CommentedConfigurationNode getMergedNode() {
        return mergedNode;
    }

    public boolean usesSplitConfig() {
        return configDir.exists() && configDir.isDirectory();
    }

    private void mergeNode(final CommentedConfigurationNode target, final CommentedConfigurationNode source) {
        try {
            for (final Map.Entry<Object, CommentedConfigurationNode> entry : source.childrenMap().entrySet()) {
                target.node(entry.getKey()).set(entry.getValue().raw());
            }
        } catch (final org.spongepowered.configurate.serialize.SerializationException e) {
            ess.getLogger().log(Level.SEVERE, "Failed to merge config nodes", e);
        }
    }

    private void initializeSplitConfigs() {
        if (!configDir.mkdirs()) {
            ess.getLogger().log(Level.WARNING, "Could not create config directory: " + configDir.getAbsolutePath());
        }
        for (final String fileName : CORE_CONFIG_FILES) {
            copyTemplate(fileName, new File(configDir, fileName));
        }
        for (final String fileName : MODULE_CONFIG_FILES.keySet()) {
            copyTemplate(fileName, new File(configDir, fileName));
        }
        ess.getLogger().log(Level.INFO, "Initialized split configuration in config/ directory");
    }

    private void migrateLegacyConfig() {
        ess.getLogger().log(Level.INFO, "Migrating legacy config.yml to split configuration files...");
        if (!configDir.mkdirs()) {
            return;
        }
        try {
            final CommentedConfigurationNode legacyNode = YamlConfigurationLoader.builder()
                    .file(legacyConfig)
                    .build()
                    .load();

            final List<String> coreKeys = getCoreKeys();
            final List<String> homesKeys = Arrays.asList(
                    "update-bed-at-daytime", "world-home-permissions", "sethome-multiple",
                    "compass-towards-home-perm", "spawn-if-no-home", "confirm-home-overwrite"
            );
            final List<String> economyKeys = Arrays.asList(
                    "starting-balance", "command-costs", "currency-symbol", "currency-symbol-suffix",
                    "max-money", "min-money", "economy-log-enabled", "economy-log-uuids",
                    "economy-log-update-enabled", "minimum-pay-amount", "pay-excludes-ignore-list",
                    "show-zero-baltop", "baltop-requirements", "baltop-entry-limit",
                    "sell-multipliers"
            );
            final List<String> helpKeys = Arrays.asList("non-ess-in-help", "hide-permissionless-help");
            final List<String> spawnKeys = Arrays.asList(
                    "newbies", "respawn-listener-priority", "spawn-join-listener-priority",
                    "respawn-at-home", "respawn-at-home-bed", "respawn-at-anchor",
                    "random-spawn-location", "random-respawn-location", "spawn-on-join"
            );

            writeKeysToFile("core.yml", legacyNode, coreKeys);
            writeKeysToFile("homes.yml", legacyNode, homesKeys);
            writeKeysToFile("economy.yml", legacyNode, economyKeys);
            writeKeysToFile("help.yml", legacyNode, helpKeys);

            if (legacyNode.hasChild("chat")) {
                writeSectionToFile("chat.yml", "chat", legacyNode.node("chat"));
            } else {
                copyTemplate("chat.yml", new File(configDir, "chat.yml"));
            }
            if (legacyNode.hasChild("protect")) {
                writeSectionToFile("protect.yml", "protect", legacyNode.node("protect"));
            } else {
                copyTemplate("protect.yml", new File(configDir, "protect.yml"));
            }
            copyTemplate("antibuild.yml", new File(configDir, "antibuild.yml"));
            writeKeysToFile("spawn.yml", legacyNode, spawnKeys);

            final File backup = new File(ess.getDataFolder(), "config.yml.migrated");
            Files.move(legacyConfig.toPath(), backup.toPath());
            ess.getLogger().log(Level.INFO, "Migration complete. Legacy config backed up to config.yml.migrated");
        } catch (final ConfigurateException e) {
            ess.getLogger().log(Level.SEVERE, "Failed to migrate legacy config.yml", e);
        } catch (final IOException e) {
            ess.getLogger().log(Level.SEVERE, "Failed to migrate legacy config.yml", e);
        }
    }

    private List<String> getCoreKeys() {
        final List<String> keys = new ArrayList<>();
        try {
            final CommentedConfigurationNode template = YamlConfigurationLoader.builder()
                    .file(getTemplateStream("core.yml"))
                    .build()
                    .load();
            for (final Object key : template.childrenMap().keySet()) {
                keys.add(key.toString());
            }
        } catch (final Exception e) {
            ess.getLogger().log(Level.WARNING, "Could not read core.yml template for migration keys", e);
        }
        return keys;
    }

    private void writeKeysToFile(final String fileName, final CommentedConfigurationNode source, final List<String> keys) throws ConfigurateException {
        final CommentedConfigurationNode target = YamlConfigurationLoader.builder().build().createNode();
        for (final String key : keys) {
            if (source.hasChild(key)) {
                target.node(key).set(source.node(key).raw());
            }
        }
        YamlConfigurationLoader.builder()
                .file(new File(configDir, fileName))
                .build()
                .save(target);
    }

    private void writeSectionToFile(final String fileName, final String sectionKey, final CommentedConfigurationNode section) throws ConfigurateException {
        final CommentedConfigurationNode target = YamlConfigurationLoader.builder().build().createNode();
        target.node(sectionKey).set(section.raw());
        YamlConfigurationLoader.builder()
                .file(new File(configDir, fileName))
                .build()
                .save(target);
    }

    private void copyTemplate(final String fileName, final File destination) {
        try (final InputStream in = ess.getResource("config/" + fileName)) {
            if (in == null) {
                ess.getLogger().log(Level.WARNING, "Missing config template: config/" + fileName);
                return;
            }
            Files.copy(in, destination.toPath());
        } catch (final IOException e) {
            ess.getLogger().log(Level.SEVERE, "Failed to copy config template: " + fileName, e);
        }
    }

    private File getTemplateStream(final String fileName) throws IOException {
        final File temp = File.createTempFile("essy-config-", ".yml");
        temp.deleteOnExit();
        try (final InputStream in = ess.getResource("config/" + fileName)) {
            if (in != null) {
                Files.copy(in, temp.toPath());
            }
        }
        return temp;
    }
}
