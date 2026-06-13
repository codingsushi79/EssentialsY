package net.essentialsy.module;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentialsModule;
import com.earth2me.essentials.antibuild.AntiBuildModule;
import com.earth2me.essentials.chat.ChatModule;
import com.earth2me.essentials.geoip.GeoIPModule;
import com.earth2me.essentials.protect.ProtectModule;
import com.earth2me.essentials.spawn.SpawnModule;
import net.essentialsx.discord.DiscordModule;
import net.essentialsx.discordlink.DiscordLinkModule;
import net.essentialsy.config.ModulesConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Central registry for all EssentialsY feature modules.
 * Modules are enabled/disabled via modules.yml without separate plugin JARs.
 */
public class ModuleManager implements IConf {
    private static final List<ModuleType> ENABLE_ORDER = Arrays.asList(
            ModuleType.CHAT,
            ModuleType.SPAWN,
            ModuleType.PROTECT,
            ModuleType.ANTIBUILD,
            ModuleType.GEOIP,
            ModuleType.DISCORD,
            ModuleType.DISCORD_LINK
    );

    private final Essentials ess;
    private final ModulesConfig modulesConfig;
    private final Map<ModuleType, Supplier<EssentialsYModule>> moduleFactories = new EnumMap<>(ModuleType.class);
    private final Map<ModuleType, EssentialsYModule> modules = new EnumMap<>(ModuleType.class);
    private final List<EssentialsYModule> activeModules = new ArrayList<>();
    private Set<String> disabledModuleCommands = Collections.emptySet();

    public ModuleManager(final Essentials ess, final ModulesConfig modulesConfig) {
        this.ess = ess;
        this.modulesConfig = modulesConfig;
        moduleFactories.put(ModuleType.CHAT, () -> new ChatModule(ess));
        moduleFactories.put(ModuleType.SPAWN, () -> new SpawnModule(ess));
        moduleFactories.put(ModuleType.PROTECT, () -> new ProtectModule(ess));
        moduleFactories.put(ModuleType.ANTIBUILD, () -> new AntiBuildModule(ess));
        moduleFactories.put(ModuleType.GEOIP, () -> new GeoIPModule(ess));
        moduleFactories.put(ModuleType.DISCORD, () -> new DiscordModule(ess));
        moduleFactories.put(ModuleType.DISCORD_LINK, () -> new DiscordLinkModule(ess));
        rebuildDisabledCommandCache();
    }

    private EssentialsYModule getOrCreateModule(final ModuleType type) {
        return modules.computeIfAbsent(type, key -> {
            final Supplier<EssentialsYModule> factory = moduleFactories.get(key);
            return factory == null ? null : factory.get();
        });
    }

    public void enableAll() {
        final List<String> skipped = new ArrayList<>();
        for (final ModuleType type : ENABLE_ORDER) {
            if (!modulesConfig.isEnabled(type)) {
                skipped.add(type.getConfigKey());
                continue;
            }
            if (type == ModuleType.DISCORD_LINK && !modulesConfig.isEnabled(ModuleType.DISCORD)) {
                skipped.add(type.getConfigKey());
                ess.getLogger().log(Level.WARNING, "Skipping discordlink module — discord module is disabled in modules.yml");
                continue;
            }
            if (type == ModuleType.DISCORD_LINK) {
                final EssentialsYModule discord = modules.get(ModuleType.DISCORD);
                if (discord == null || !discord.isEnabled()) {
                    skipped.add(type.getConfigKey());
                    ess.getLogger().log(Level.WARNING, "Skipping discordlink module — discord module failed to start");
                    continue;
                }
            }
            final EssentialsYModule module = getOrCreateModule(type);
            if (module == null) {
                continue;
            }
            try {
                module.enable();
                activeModules.add(module);
            } catch (final Exception e) {
                ess.getLogger().log(Level.SEVERE, "Failed to enable module: " + type.getConfigKey(), e);
            }
        }
        if (!skipped.isEmpty()) {
            ess.getLogger().log(Level.INFO, "Skipped disabled modules: " + String.join(", ", skipped));
        }
    }

    public void disableAll() {
        for (int i = activeModules.size() - 1; i >= 0; i--) {
            activeModules.get(i).disable();
        }
        activeModules.clear();
    }

    @Override
    public void reloadConfig() {
        disableAll();
        rebuildDisabledCommandCache();
        enableAll();
    }

    private void rebuildDisabledCommandCache() {
        final Set<String> disabled = new HashSet<>();
        for (final ModuleType type : ModuleType.values()) {
            if (!modulesConfig.isEnabled(type)) {
                for (final String command : type.getCommands()) {
                    disabled.add(command.toLowerCase(Locale.ENGLISH));
                }
            }
        }
        if (!modulesConfig.isEnabled(ModuleType.DISCORD)) {
            for (final String command : ModuleType.DISCORD_LINK.getCommands()) {
                disabled.add(command.toLowerCase(Locale.ENGLISH));
            }
        }
        disabledModuleCommands = Collections.unmodifiableSet(disabled);
    }

    public boolean isModuleEnabled(final ModuleType type) {
        if (!modulesConfig.isEnabled(type)) {
            return false;
        }
        final EssentialsYModule module = modules.get(type);
        return module != null && module.isEnabled();
    }

    public Optional<EssentialsYModule> getModule(final ModuleType type) {
        return Optional.ofNullable(modules.get(type));
    }

    public Optional<EssentialsYModule> findModuleForCommand(final String commandName) {
        final String lower = commandName.toLowerCase(Locale.ENGLISH);
        for (final EssentialsYModule module : activeModules) {
            if (module.handlesCommand(lower)) {
                return Optional.of(module);
            }
        }
        return Optional.empty();
    }

    public boolean isModuleCommandDisabled(final String commandName) {
        return disabledModuleCommands.contains(commandName.toLowerCase(Locale.ENGLISH));
    }

    public IEssentialsModule getModuleContext(final String commandName) {
        return findModuleForCommand(commandName)
                .map(EssentialsYModule::getModuleContext)
                .orElse(null);
    }

    public String getCommandPath(final String commandName) {
        return findModuleForCommand(commandName)
                .map(EssentialsYModule::getCommandPath)
                .orElse("com.earth2me.essentials.commands.Command");
    }

    public ClassLoader getCommandClassLoader(final String commandName) {
        return findModuleForCommand(commandName)
                .map(EssentialsYModule::getClassLoader)
                .orElse(Essentials.class.getClassLoader());
    }

    public List<EssentialsYModule> getActiveModules() {
        return Collections.unmodifiableList(new ArrayList<>(activeModules));
    }
}
