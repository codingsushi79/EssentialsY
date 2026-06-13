package com.earth2me.essentials.spawn;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentialsModule;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SpawnModule extends AbstractModule {
    private SpawnStorage spawns;
    private EssentialsSpawnPlayerListener playerListener;

    public SpawnModule(final Essentials ess) {
        super(ess, ModuleType.SPAWN);
    }

    @Override
    public String getCommandPath() {
        return "com.earth2me.essentials.spawn.Command";
    }

    @Override
    public IEssentialsModule getModuleContext() {
        return spawns;
    }

    @Override
    protected void onEnable() {
        spawns = new SpawnStorage(ess);
        ess.addReloadListener(spawns);
        playerListener = new EssentialsSpawnPlayerListener(ess, spawns);

        final EventPriority respawnPriority = ess.getSettings().getRespawnPriority();
        if (respawnPriority != null) {
            ess.getServer().getPluginManager().registerEvent(PlayerRespawnEvent.class, playerListener, respawnPriority,
                    (ll, event) -> ((EssentialsSpawnPlayerListener) ll).onPlayerRespawn((PlayerRespawnEvent) event), ess);
        }

        final EventPriority joinPriority = ess.getSettings().getSpawnJoinPriority();
        if (joinPriority != null) {
            ess.getServer().getPluginManager().registerEvent(PlayerJoinEvent.class, playerListener, joinPriority,
                    (ll, event) -> ((EssentialsSpawnPlayerListener) ll).onPlayerJoin((PlayerJoinEvent) event), ess);
        }
    }

    @Override
    protected void onDisable() {
        if (playerListener != null) {
            HandlerList.unregisterAll(playerListener);
            playerListener = null;
        }
        spawns = null;
    }
}
