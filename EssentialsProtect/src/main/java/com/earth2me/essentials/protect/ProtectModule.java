package com.earth2me.essentials.protect;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.utils.VersionUtil;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ProtectModule extends AbstractModule {
    private ProtectAdapter adapter;
    private final List<Listener> listeners = new ArrayList<>();

    public ProtectModule(final Essentials ess) {
        super(ess, ModuleType.PROTECT);
    }

    @Override
    public String getCommandPath() {
        return null;
    }

    @Override
    protected void onEnable() {
        adapter = new ProtectAdapter();
        new EssentialsConnect(ess, adapter);
        registerListener(new EssentialsProtectBlockListener(adapter));
        registerListener(new EssentialsProtectEntityListener(adapter));
        if (VersionUtil.getServerBukkitVersion().isHigherThan(VersionUtil.v1_13_2_R01)) {
            registerListener(new EssentialsProtectEntityListener_1_13_2_R1(adapter));
        }
        if (VersionUtil.getServerBukkitVersion().isHigherThan(VersionUtil.v1_14_R01)) {
            registerListener(new EssentialsProtectEntityListener_1_14_R1(adapter));
        }
        if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_16_1_R01)) {
            registerListener(new EssentialsProtectBlockListener_1_16_R1(adapter));
        }
        if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_21_3_R01)) {
            registerListener(new EssentialsProtectEntityListener_1_21_3_R1(adapter));
        }
        registerListener(new EssentialsProtectWeatherListener(adapter));
    }

    private void registerListener(final Listener listener) {
        ess.getServer().getPluginManager().registerEvents(listener, ess);
        listeners.add(listener);
    }

    @Override
    protected void onDisable() {
        for (final Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
        listeners.clear();
        adapter = null;
    }

    final class ProtectAdapter implements IProtect {
        private final Map<ProtectConfig, Boolean> settingsBoolean = new EnumMap<>(ProtectConfig.class);
        private final Map<ProtectConfig, String> settingsString = new EnumMap<>(ProtectConfig.class);
        private final Map<ProtectConfig, List<Material>> settingsList = new EnumMap<>(ProtectConfig.class);

        @Override
        public EssentialsConnect getEssentialsConnect() {
            return null;
        }

        @Override
        public Map<ProtectConfig, Boolean> getSettingsBoolean() {
            return settingsBoolean;
        }

        @Override
        public Map<ProtectConfig, String> getSettingsString() {
            return settingsString;
        }

        @Override
        public Map<ProtectConfig, List<Material>> getSettingsList() {
            return settingsList;
        }

        @Override
        public boolean getSettingBool(final ProtectConfig protectConfig) {
            final Boolean bool = settingsBoolean.get(protectConfig);
            return bool == null ? protectConfig.getDefaultValueBoolean() : bool;
        }

        @Override
        public String getSettingString(final ProtectConfig protectConfig) {
            final String str = settingsString.get(protectConfig);
            return str == null ? protectConfig.getDefaultValueString() : str;
        }
    }
}
