package com.earth2me.essentials.geoip;

import com.earth2me.essentials.Essentials;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;
import org.bukkit.event.HandlerList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GeoIPModule extends AbstractModule {
    private EssentialsGeoIPPlayerListener listener;

    public GeoIPModule(final Essentials ess) {
        super(ess, ModuleType.GEOIP);
    }

    @Override
    public String getCommandPath() {
        return null;
    }

    @Override
    protected void onEnable() {
        Logger.getLogger(com.fasterxml.jackson.databind.ext.Java7Support.class.getName()).setLevel(Level.SEVERE);
        listener = new EssentialsGeoIPPlayerListener(ess.getDataFolder(), ess, ess);
        ess.getServer().getPluginManager().registerEvents(listener, ess);
        ess.getLogger().log(Level.INFO, "GeoIP module enabled. Includes GeoLite2 data from MaxMind (http://www.maxmind.com/)");
    }

    @Override
    protected void onDisable() {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
    }
}
