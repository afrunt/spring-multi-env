package com.afrunt.spring.multienv.ctx.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class SimpleBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBean.class);
    private final String envProp;

    public SimpleBean(@Value("${envProp}") String envProp) {
        this.envProp = envProp;
    }

    public String getEnvProp() {
        return envProp;
    }

    @PostConstruct
    public void start() {
        LOGGER.info("SimpleBean created");
    }

    @PreDestroy
    public void stop() {
        LOGGER.info("SimpleBean destroyed");
    }
}
