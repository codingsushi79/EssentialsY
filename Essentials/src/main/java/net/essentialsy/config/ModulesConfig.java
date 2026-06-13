package net.essentialsy.config;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.config.EssentialsConfiguration;
import net.essentialsy.module.ModuleType;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.File;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Loads module enable/disable toggles from modules.yml.
 */
public class ModulesConfig implements IConf {
    private final Essentials ess;
    private final EssentialsConfiguration config;
    private final Map<ModuleType, Boolean> enabled = new EnumMap<>(ModuleType.class);

    public ModulesConfig(final Essentials ess) {
        this.ess = ess;
        this.config = new EssentialsConfiguration(
                new File(ess.getDataFolder(), "modules.yml"),
                "/modules.yml"
        );
    }

    @Override
    public void reloadConfig() {
        config.load();
        enabled.clear();
        final CommentedConfigurationNode root = config.getRootNode();
        for (final ModuleType type : ModuleType.values()) {
            final boolean defaultValue = type.isDefaultEnabled();
            final boolean value = root.node("modules", type.getConfigKey(), "enabled").getBoolean(defaultValue);
            enabled.put(type, value);
        }
        ess.getLogger().log(Level.INFO, "Loaded module configuration from modules.yml");
    }

    public boolean isEnabled(final ModuleType type) {
        return enabled.getOrDefault(type, type.isDefaultEnabled());
    }

    public Map<ModuleType, Boolean> getAll() {
        return Collections.unmodifiableMap(enabled);
    }

    public EssentialsConfiguration getConfig() {
        return config;
    }
}
