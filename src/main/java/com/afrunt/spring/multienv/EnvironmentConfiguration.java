package com.afrunt.spring.multienv;

import org.springframework.core.env.CompositePropertySource;

public interface EnvironmentConfiguration {
    String getEnvironmentId();

    CompositePropertySource getPropertySource();
}
