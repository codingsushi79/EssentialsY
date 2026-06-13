package net.essentialsx.discord;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.metrics.MetricsWrapper;
import net.essentialsx.discord.interactions.InteractionControllerImpl;
import net.essentialsy.module.ModuleManager;
import net.essentialsy.module.ModuleType;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tlLiteral;

/**
 * Internal Discord module runtime — replaces the standalone DiscordBridge plugin.
 */
public class DiscordBridge {
    private final Essentials ess;
    private MetricsWrapper metrics;
    private JDADiscordService jda;
    private DiscordSettings settings;
    private boolean isPAPI;
    private boolean enabled;

    public DiscordBridge(final Essentials ess) {
        this.ess = ess;
    }

    public void enable() {
        if (enabled) {
            return;
        }

        final String[] javaVersion = System.getProperty("java.version").split("\\.");
        if (Runtime.getRuntime().availableProcessors() <= 1 && javaVersion[0].startsWith("17")
                && (javaVersion.length < 2 || (javaVersion[1].equals("0") && javaVersion[2].startsWith("1")))) {
            ess.getLogger().log(Level.INFO, "EssentialsY is mitigating JDK-8274349");
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "1");
        }

        isPAPI = ess.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;

        settings = new DiscordSettings(this);
        ess.addReloadListener(settings);

        if (metrics == null) {
            metrics = new MetricsWrapper(ess, 9824, false);
        }

        jda = new JDADiscordService(this);
        try {
            jda.startup();
            ess.scheduleSyncDelayedTask(() -> ((InteractionControllerImpl) jda.getInteractionController()).processBatchRegistration());
        } catch (Exception e) {
            ess.getLogger().log(Level.SEVERE, ess.getAdventureFacet().miniToLegacy(tlLiteral("discordErrorLogin", e.getMessage())));
            if (ess.getSettings().isDebug()) {
                e.printStackTrace();
            }
            if (jda != null) {
                jda.shutdown();
            }
            throw new IllegalStateException("Discord module failed to start", e);
        }
        enabled = true;
    }

    public void disable() {
        if (!enabled) {
            return;
        }
        if (jda != null && !jda.isInvalidStartup()) {
            jda.shutdown();
        }
        jda = null;
        enabled = false;
    }

    public void onReload() {
        if (jda != null && !jda.isInvalidStartup()) {
            jda.updateListener();
            jda.updatePresence();
            jda.updatePrimaryChannel();
            jda.updateConsoleRelay();
            jda.updateTypesRelay();
        }
    }

    public static Logger getWrappedLogger() {
        return Logger.getLogger("EssentialsY:Discord");
    }

    public boolean isInvalidStartup() {
        return jda != null && jda.isInvalidStartup();
    }

    public Essentials getEss() {
        return ess;
    }

    public DiscordSettings getSettings() {
        return settings;
    }

    public JDADiscordService getJda() {
        return jda;
    }

    public boolean isPAPI() {
        return isPAPI;
    }

    public boolean isEssentialsChat() {
        final ModuleManager manager = ess.getModuleManager();
        return manager != null && manager.isModuleEnabled(ModuleType.CHAT);
    }

    public File getDataFolder() {
        return ess.getDataFolder();
    }

    public Logger getLogger() {
        return ess.getLogger();
    }

    public boolean isEnabled() {
        return enabled && jda != null && !jda.isInvalidStartup();
    }
}
