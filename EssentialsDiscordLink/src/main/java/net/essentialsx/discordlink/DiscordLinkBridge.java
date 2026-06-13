package net.essentialsx.discordlink;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.metrics.MetricsWrapper;
import com.google.common.collect.ImmutableSet;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discord.InteractionException;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import net.essentialsx.discord.DiscordBridge;
import net.essentialsx.discordlink.commands.discord.AccountInteractionCommand;
import net.essentialsx.discordlink.commands.discord.LinkInteractionCommand;
import net.essentialsx.discordlink.commands.discord.UnlinkInteractionCommand;
import net.essentialsx.discordlink.listeners.LinkBukkitListener;
import net.essentialsx.discordlink.rolesync.RoleSyncManager;
import org.bukkit.plugin.ServicePriority;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Internal DiscordLink module runtime — replaces the standalone DiscordLinkBridge plugin.
 */
public class DiscordLinkBridge {
    private final Essentials ess;
    private final DiscordBridge discord;
    private MetricsWrapper metrics;
    private DiscordService api;
    private DiscordLinkSettings settings;
    private AccountStorage accounts;
    private AccountLinkManager linkManager;
    private RoleSyncManager roleSyncManager;
    private boolean enabled;

    public DiscordLinkBridge(final Essentials ess, final DiscordBridge discord) {
        this.ess = ess;
        this.discord = discord;
    }

    public void enable() {
        if (enabled) {
            return;
        }
        if (discord == null || !discord.isEnabled()) {
            throw new IllegalStateException("Discord module must be enabled before DiscordLink");
        }

        api = ess.getServer().getServicesManager().load(DiscordService.class);
        if (api == null) {
            throw new IllegalStateException("DiscordService not registered");
        }

        settings = new DiscordLinkSettings(this);
        ess.addReloadListener(settings);
        try {
            accounts = new AccountStorage(this);
        } catch (IOException e) {
            ess.getLogger().log(Level.SEVERE, "Unable to create link accounts file", e);
            throw new IllegalStateException("DiscordLink module failed to start", e);
        }

        roleSyncManager = new RoleSyncManager(this);
        linkManager = new AccountLinkManager(this, accounts, roleSyncManager);

        ess.getServer().getPluginManager().registerEvents(new LinkBukkitListener(this), ess);
        ess.getServer().getServicesManager().register(DiscordLinkService.class, linkManager, ess, ServicePriority.Normal);

        if (!(api.getInteractionController().getCommand("link") instanceof LinkInteractionCommand)) {
            try {
                api.getInteractionController().registerCommand(new AccountInteractionCommand(linkManager));
                api.getInteractionController().registerCommand(new LinkInteractionCommand(linkManager));
                api.getInteractionController().registerCommand(new UnlinkInteractionCommand(linkManager));
            } catch (InteractionException e) {
                throw new IllegalStateException("DiscordLink module failed to register commands", e);
            }
        }

        ess.getPermissionsHandler().registerContext("essentials:linked", user ->
                Collections.singleton(String.valueOf(linkManager.isLinked(user.getUUID()))), () -> ImmutableSet.of("true", "false"));

        if (metrics == null) {
            metrics = new MetricsWrapper(ess, 11462, false);
        }
        enabled = true;
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        if (roleSyncManager != null) {
            roleSyncManager.shutdown();
        }
        if (accounts != null) {
            accounts.shutdown();
        }
        if (linkManager != null) {
            ess.getServer().getServicesManager().unregister(DiscordLinkService.class, linkManager);
        }
        roleSyncManager = null;
        linkManager = null;
        accounts = null;
        enabled = false;
    }

    public void onReload() {
        if (roleSyncManager != null) {
            roleSyncManager.onReload();
        }
    }

    public Essentials getEss() {
        return ess;
    }

    public org.bukkit.Server getServer() {
        return ess.getServer();
    }

    public DiscordService getApi() {
        return api;
    }

    public DiscordLinkSettings getSettings() {
        return settings;
    }

    public AccountStorage getAccountStorage() {
        return accounts;
    }

    public AccountLinkManager getLinkManager() {
        return linkManager;
    }

    public File getDataFolder() {
        return ess.getDataFolder();
    }

    public Logger getLogger() {
        return ess.getLogger();
    }

    public boolean isEnabled() {
        return enabled;
    }
}
