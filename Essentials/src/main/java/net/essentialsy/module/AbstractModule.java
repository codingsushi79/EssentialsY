package net.essentialsy.module;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentialsModule;

import java.util.Locale;
import java.util.Set;

public abstract class AbstractModule implements EssentialsYModule {
    protected final Essentials ess;
    protected final ModuleType type;
    private boolean enabled = false;

    protected AbstractModule(final Essentials ess, final ModuleType type) {
        this.ess = ess;
        this.type = type;
    }

    @Override
    public ModuleType getType() {
        return type;
    }

    @Override
    public ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    @Override
    public IEssentialsModule getModuleContext() {
        return null;
    }

    @Override
    public boolean handlesCommand(final String commandName) {
        return type.getCommands().contains(commandName.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public void enable() {
        if (enabled) {
            return;
        }
        onEnable();
        enabled = true;
        ess.getLogger().info("Enabled module: " + type.getConfigKey());
    }

    @Override
    public void disable() {
        if (!enabled) {
            return;
        }
        onDisable();
        enabled = false;
        ess.getLogger().info("Disabled module: " + type.getConfigKey());
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    @Override
    public void reloadConfig() {
        // Most modules reload via EssentialsConnect or Settings
    }
}
