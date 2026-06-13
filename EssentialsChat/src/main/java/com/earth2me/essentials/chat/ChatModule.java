package com.earth2me.essentials.chat;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.chat.processing.ChatHandler;
import com.earth2me.essentials.chat.processing.PaperChatHandler;
import com.earth2me.essentials.utils.VersionUtil;
import net.essentialsy.module.AbstractModule;
import net.essentialsy.module.ModuleType;

public class ChatModule extends AbstractModule {
    private boolean listenersRegistered = false;

    public ChatModule(final Essentials ess) {
        super(ess, ModuleType.CHAT);
    }

    @Override
    public String getCommandPath() {
        return "com.earth2me.essentials.chat.Command";
    }

    @Override
    protected void onEnable() {
        ess.getSettings().setEssentialsChatActive(true);
        if (listenersRegistered) {
            return;
        }
        if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_16_5_R01)
                && VersionUtil.isPaper()
                && ess.getSettings().isUsePaperChatEvent()) {
            new PaperChatHandler(ess, ess).registerListeners();
        } else {
            new ChatHandler(ess, ess).registerListeners();
        }
        listenersRegistered = true;
    }

    @Override
    protected void onDisable() {
        ess.getSettings().setEssentialsChatActive(false);
    }
}
