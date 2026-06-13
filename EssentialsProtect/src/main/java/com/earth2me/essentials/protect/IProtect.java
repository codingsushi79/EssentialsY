package com.earth2me.essentials.protect;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public interface IProtect {
    boolean getSettingBool(final ProtectConfig protectConfig);

    String getSettingString(final ProtectConfig protectConfig);

    EssentialsConnect getEssentialsConnect();

    Map<ProtectConfig, Boolean> getSettingsBoolean();

    Map<ProtectConfig, String> getSettingsString();

    Map<ProtectConfig, List<Material>> getSettingsList();
}
