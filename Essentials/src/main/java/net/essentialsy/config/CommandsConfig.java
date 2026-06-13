package net.essentialsy.config;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.config.EssentialsConfiguration;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Granular command enable/disable control via commands.yml.
 * Supports per-command disabling and category-wide toggles.
 */
public class CommandsConfig implements IConf {
    private final Essentials ess;
    private final EssentialsConfiguration config;
    private final Set<String> disabledCommands = new HashSet<>();
    private final Set<String> categoryDisabledCommands = new HashSet<>();

    public CommandsConfig(final Essentials ess) {
        this.ess = ess;
        this.config = new EssentialsConfiguration(
                new File(ess.getDataFolder(), "commands.yml"),
                "/commands.yml"
        );
    }

    @Override
    public void reloadConfig() {
        config.load();
        disabledCommands.clear();
        categoryDisabledCommands.clear();

        try {
            final CommentedConfigurationNode root = config.getRootNode();

            final List<String> individuallyDisabled = root.node("disabled").getList(String.class, Collections.emptyList());
            for (final String cmd : individuallyDisabled) {
                disabledCommands.add(cmd.toLowerCase(Locale.ENGLISH));
            }

            final CommentedConfigurationNode categories = root.node("categories");
            for (final Map.Entry<Object, CommentedConfigurationNode> entry : categories.childrenMap().entrySet()) {
                final String categoryName = entry.getKey().toString();
                final CommentedConfigurationNode categoryNode = entry.getValue();
                final boolean categoryEnabled = categoryNode.node("enabled").getBoolean(true);
                if (categoryEnabled) {
                    continue;
                }
                final List<String> commands = categoryNode.node("commands").getList(String.class, Collections.emptyList());
                for (final String cmd : commands) {
                    categoryDisabledCommands.add(cmd.toLowerCase(Locale.ENGLISH));
                }
                ess.getLogger().log(Level.INFO, "Command category '" + categoryName + "' is disabled (" + commands.size() + " commands)");
            }
        } catch (final org.spongepowered.configurate.serialize.SerializationException e) {
            ess.getLogger().log(Level.SEVERE, "Failed to load commands.yml", e);
        }
    }

    public boolean isCommandDisabled(final String commandName) {
        final String lower = commandName.toLowerCase(Locale.ENGLISH);
        return disabledCommands.contains(lower) || categoryDisabledCommands.contains(lower);
    }

    public Set<String> getAllDisabled() {
        final Set<String> all = new HashSet<>(disabledCommands);
        all.addAll(categoryDisabledCommands);
        return Collections.unmodifiableSet(all);
    }

    public EssentialsConfiguration getConfig() {
        return config;
    }
}
