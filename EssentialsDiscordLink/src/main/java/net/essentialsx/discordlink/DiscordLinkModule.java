package net.essentialsx.discordlink;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentialsModule;
import net.essentialsx.discord.DiscordBridge;
import net.essentialsx.discord.DiscordModule;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;

public class DiscordLinkModule extends AbstractModule implements IEssentialsModule {
    private DiscordLinkBridge bridge;

    public DiscordLinkModule(final Essentials ess) {
        super(ess, ModuleType.DISCORD_LINK);
    }

    @Override
    public String getCommandPath() {
        return "net.essentialsx.discordlink.commands.bukkit.Command";
    }

    @Override
    public IEssentialsModule getModuleContext() {
        return bridge != null ? bridge.getLinkManager() : null;
    }

    @Override
    protected void onEnable() {
        final DiscordModule discordModule = (DiscordModule) ess.getModuleManager()
                .getModule(ModuleType.DISCORD)
                .orElse(null);
        if (discordModule == null || !discordModule.isEnabled()) {
            throw new IllegalStateException("Discord module must be enabled before DiscordLink");
        }
        final DiscordBridge discordBridge = discordModule.getBridge();
        bridge = new DiscordLinkBridge(ess, discordBridge);
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
