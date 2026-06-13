package net.essentialsx.discord.util;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shared scheduler for all webhook dispatchers — one thread pool instead of one per channel.
 */
public final class WebhookSchedulerPool {
    private static final long TICK_MS = 250;
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        final Thread t = new Thread(r, "EssentialsY-WebhookPool");
        t.setDaemon(true);
        return t;
    });
    private static final Set<WebhookDispatcher> DISPATCHERS = ConcurrentHashMap.newKeySet();

    static {
        SCHEDULER.scheduleAtFixedRate(() -> {
            for (final WebhookDispatcher dispatcher : DISPATCHERS) {
                dispatcher.drainTick();
            }
        }, TICK_MS, TICK_MS, TimeUnit.MILLISECONDS);
    }

    private WebhookSchedulerPool() {
    }

    static void register(final WebhookDispatcher dispatcher) {
        DISPATCHERS.add(dispatcher);
    }

    static void unregister(final WebhookDispatcher dispatcher) {
        DISPATCHERS.remove(dispatcher);
    }
}
