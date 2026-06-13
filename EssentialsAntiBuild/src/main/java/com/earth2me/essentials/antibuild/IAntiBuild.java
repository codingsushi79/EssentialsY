package com.earth2me.essentials.antibuild;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public interface IAntiBuild {
    boolean checkProtectionItems(final AntiBuildConfig list, final Material mat);

    boolean checkProtectionItems(final AntiBuildConfig list, final String mat);

    boolean getSettingBool(final AntiBuildConfig protectConfig);

    EssentialsConnect getEssentialsConnect();

    Map<AntiBuildConfig, Boolean> getSettingsBoolean();

    Map<AntiBuildConfig, List<Material>> getSettingsList();
}
