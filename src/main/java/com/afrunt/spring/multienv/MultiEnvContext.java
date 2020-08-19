package com.afrunt.spring.multienv;

import java.util.HashMap;
import java.util.Map;

public class MultiEnvContext implements AutoCloseable {
    private final Map<String, ContextBuilder> contextBuildersMap;
    private final Map<String, EnvContext> envContextMap = new HashMap<>();
    private boolean started = false;
    private boolean lazyInitialization = false;

    public MultiEnvContext(Map<String, ContextBuilder> contextBuildersMap) {
        this.contextBuildersMap = new HashMap<>(contextBuildersMap);
    }

    public synchronized void start() {
        if (started) {
            return;
        }

        if (!lazyInitialization) {
            contextBuildersMap.forEach((key, value) -> envContextMap.put(key, new EnvContext(key, value.build())));
        }

        started = true;
    }

    public <T> T getBean(String envId, Class<T> type) {
        return envContextMap.get(envId).getBean(type);
    }

    @Override
    public synchronized void close() {
        envContextMap
                .forEach((envId, envContext) -> envContext.close());

        envContextMap.clear();

        started = false;
    }

    public boolean isStarted() {
        return started;
    }
}
