package net.essentialsy.module;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Built-in EssentialsY feature modules. Each module can be toggled in modules.yml.
 */
public enum ModuleType {
    CHAT("chat", "Chat formatting and local chat", false,
            "toggleshout"),
    SPAWN("spawn", "Spawn points and new player handling", false,
            "spawn", "setspawn"),
    PROTECT("protect", "World protection and damage prevention", false),
    ANTIBUILD("antibuild", "Build and item-use restrictions", false),
    GEOIP("geoip", "GeoIP country lookup on join", false),
    DISCORD("discord", "Discord bridge integration", false,
            "discord", "discordbroadcast"),
    DISCORD_LINK("discordlink", "Discord account linking", false,
            "link", "unlink"),
    XMPP("xmpp", "XMPP messaging integration", false,
            "xmpp", "setxmpp", "xmppspy");

    private final String configKey;
    private final String description;
    private final boolean defaultEnabled;
    private final Set<String> commands;

    ModuleType(final String configKey, final String description, final boolean defaultEnabled, final String... commands) {
        this.configKey = configKey;
        this.description = description;
        this.defaultEnabled = defaultEnabled;
        this.commands = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(commands)));
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public Set<String> getCommands() {
        return commands;
    }

    public static ModuleType fromConfigKey(final String key) {
        for (final ModuleType type : values()) {
            if (type.configKey.equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}
