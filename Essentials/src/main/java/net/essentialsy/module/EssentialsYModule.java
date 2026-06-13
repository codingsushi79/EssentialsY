package net.essentialsy.module;

import com.earth2me.essentials.IConf;
import com.earth2me.essentials.IEssentialsModule;

/**
 * Internal feature module loaded by {@link ModuleManager}.
 */
public interface EssentialsYModule extends IConf {

    ModuleType getType();

    void enable();

    void disable();

    /**
     * ClassLoader used to load this module's command classes.
     */
    ClassLoader getClassLoader();

    /**
     * Package prefix for command classes, e.g. {@code com.earth2me.essentials.spawn.Command}.
     */
    String getCommandPath();

    /**
     * Optional module context passed to commands (e.g. SpawnStorage).
     */
    IEssentialsModule getModuleContext();

    boolean handlesCommand(String commandName);

    boolean isEnabled();
}
