package com.earth2me.essentials.antibuild;

import com.earth2me.essentials.Essentials;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AntiBuildModule extends AbstractModule {
    private AntiBuildAdapter adapter;
    private EssentialsAntiBuildListener listener;

    public AntiBuildModule(final Essentials ess) {
        super(ess, ModuleType.ANTIBUILD);
    }

    @Override
    public String getCommandPath() {
        return null;
    }

    @Override
    protected void onEnable() {
        adapter = new AntiBuildAdapter();
        new EssentialsConnect(ess, adapter);
        listener = new EssentialsAntiBuildListener(adapter, ess);
        ess.getServer().getPluginManager().registerEvents(listener, ess);
    }

    @Override
    protected void onDisable() {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
        adapter = null;
    }

    final class AntiBuildAdapter implements IAntiBuild {
        private final Map<AntiBuildConfig, Boolean> settingsBoolean = new EnumMap<>(AntiBuildConfig.class);
        private final Map<AntiBuildConfig, List<Material>> settingsList = new EnumMap<>(AntiBuildConfig.class);

        @Override
        public boolean checkProtectionItems(final AntiBuildConfig list, final Material mat) {
            final List<Material> itemList = settingsList.get(list);
            return itemList != null && !itemList.isEmpty() && itemList.contains(mat);
        }

        @Override
        public boolean checkProtectionItems(final AntiBuildConfig list, final String mat) {
            final List<String> protectList = ess.getSettings().getProtectListRaw(list.getConfigName());
            return protectList != null && !protectList.isEmpty() && protectList.contains(mat);
        }

        @Override
        public EssentialsConnect getEssentialsConnect() {
            return null;
        }

        @Override
        public Map<AntiBuildConfig, Boolean> getSettingsBoolean() {
            return settingsBoolean;
        }

        @Override
        public Map<AntiBuildConfig, List<Material>> getSettingsList() {
            return settingsList;
        }

        @Override
        public boolean getSettingBool(final AntiBuildConfig protectConfig) {
            final Boolean bool = settingsBoolean.get(protectConfig);
            return bool == null ? protectConfig.getDefaultValueBoolean() : bool;
        }
    }
}
