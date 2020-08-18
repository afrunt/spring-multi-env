package com.afrunt.spring.multienv.ctx.simple;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimpleBean {
    private final String envProp;

    public SimpleBean(@Value("${envProp}") String envProp) {
        this.envProp = envProp;
    }

    public String getEnvProp() {
        return envProp;
    }
}
