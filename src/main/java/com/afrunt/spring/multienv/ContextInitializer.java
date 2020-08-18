package com.afrunt.spring.multienv;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.List;

public interface ContextInitializer {
    ApplicationContext initialize(EnvironmentConfiguration configuration);

    default List<String> getActiveProfiles() {
        return Collections.emptyList();
    }

    default void populatePropertySource(ApplicationContext applicationContext, CompositePropertySource propertySource) {
        ConfigurableEnvironment environment = (ConfigurableEnvironment) applicationContext.getEnvironment();
        environment
                .getPropertySources()
                .addLast(propertySource);
    }
}
