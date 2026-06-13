package net.essentialsx.discord;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentialsModule;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;

public class DiscordModule extends AbstractModule implements IEssentialsModule {
    private DiscordBridge bridge;

    public DiscordModule(final Essentials ess) {
        super(ess, ModuleType.DISCORD);
    }

    @Override
    public String getCommandPath() {
        return "net.essentialsx.discord.commands.Command";
    }

    @Override
    public IEssentialsModule getModuleContext() {
        return bridge != null ? bridge.getJda() : null;
    }

    public DiscordBridge getBridge() {
        return bridge;
    }

    @Override
    protected void onEnable() {
        bridge = new DiscordBridge(ess);
        bridge.enable();
    }

    @Override
    protected void onDisable() {
        if (bridge != null) {
            bridge.disable();
            bridge = null;
        }
    }

    @Override
    public void reloadConfig() {
        if (bridge != null) {
            bridge.onReload();
        }
    }
}
