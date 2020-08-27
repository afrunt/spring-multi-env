package com.afrunt.spring.multienv;

import org.springframework.context.support.GenericApplicationContext;

import java.util.*;

public class MultiEnvContext implements AutoCloseable {
    private final Map<String, ContextBuilder<? extends GenericApplicationContext>> contextBuildersMap;
    private final Map<String, EnvContext> envContextMap = new HashMap<>();
    private boolean started = false;
    private boolean lazyInitialization = false;

    public MultiEnvContext(Map<String, ContextBuilder<? extends GenericApplicationContext>> contextBuildersMap) {
        this.contextBuildersMap = new HashMap<>(contextBuildersMap);
    }

    public List<String> environmentNames() {
        return new ArrayList<>(new TreeSet<>(contextBuildersMap.keySet()));
    }

    public synchronized void start() {
        if (started) {
            return;
        }

        if (!lazyInitialization) {
            contextBuildersMap.forEach((key, value) -> startEnvironmentContext(key));
        }

        started = true;
    }

    public synchronized void startEnvironmentContext(String envId) {
        if (!contextBuildersMap.containsKey(envId)) {
            throw new IllegalArgumentException("Environment " + envId + " is unknown");
        }

        if (!envContextMap.containsKey(envId)) {
            ContextBuilder<? extends GenericApplicationContext> builder = contextBuildersMap.get(envId);
            envContextMap.put(envId, new EnvContext(envId, builder.build()));
        }
    }

    public MultiEnvContext lazyInitialization() {
        this.lazyInitialization = true;
        return this;
    }

    public MultiEnvContext eagerInitialization() {
        this.lazyInitialization = false;
        return this;
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
