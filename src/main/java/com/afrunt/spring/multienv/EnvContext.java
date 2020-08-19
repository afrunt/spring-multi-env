package com.afrunt.spring.multienv;

import org.springframework.context.support.GenericApplicationContext;

public class EnvContext implements AutoCloseable {
    private final String environmentId;
    private final GenericApplicationContext ctx;

    public EnvContext(String environmentId, GenericApplicationContext ctx) {
        this.environmentId = environmentId;
        this.ctx = ctx;
    }

    public <T> T getBean(Class<T> type) {
        return ctx.getBean(type);
    }

    @Override
    public void close() {
        ctx.close();
    }
}
