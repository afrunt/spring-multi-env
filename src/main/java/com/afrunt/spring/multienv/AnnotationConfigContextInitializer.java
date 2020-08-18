package com.afrunt.spring.multienv;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AnnotationConfigContextInitializer implements ContextInitializer {
    private List<String> basePackages = new ArrayList<>();
    private List<Class<?>> componentClasses = new ArrayList<>();

    @Override
    public ApplicationContext initialize(EnvironmentConfiguration configuration) {
        validateConfiguration(configuration);

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().setActiveProfiles(getActiveProfiles().toArray(new String[]{}));
        populatePropertySource(ctx, configuration.getPropertySource());

        if (!getBasePackages().isEmpty()) {
            ctx.scan(getBasePackages().toArray(new String[]{}));
        }

        if (!getComponentClasses().isEmpty()) {
            ctx.register(getComponentClasses().toArray(new Class[]{}));
        }

        ctx.refresh();

        return ctx;
    }

    public Collection<String> getBasePackages() {
        return basePackages;
    }

    public List<Class<?>> getComponentClasses() {
        return componentClasses;
    }

    public AnnotationConfigContextInitializer basePackages(String... basePackages) {
        Assert.notNull(basePackages, "basePackages cannot be null");
        Assert.noNullElements(basePackages, "basePackage cannot be null");

        this.basePackages = Arrays.stream(basePackages)
                .collect(Collectors.toList());
        return this;
    }

    public AnnotationConfigContextInitializer componentClasses(Class<?>... componentClasses) {
        Assert.notNull(componentClasses, "componentClasses cannot be null");
        Assert.noNullElements(componentClasses, "componentClass cannot be null");

        this.componentClasses =  Arrays.stream(componentClasses)
                .collect(Collectors.toList());

        return this;
    }

    private void validateConfiguration(EnvironmentConfiguration configuration) {
        Assert.notNull(configuration, "Configuration cannot be null");
        Assert.hasText(configuration.getEnvironmentId(), "environmentId cannot be empty");
        Assert.notNull(configuration.getPropertySource(), "propertySource cannot be null");
        Assert.notNull(basePackages, "basePackages cannot be null");
        Assert.notNull(componentClasses, "componentClasses cannot be null");

        if (basePackages.isEmpty() && componentClasses.isEmpty()) {
            throw new IllegalStateException("Either basePackages or componentClasses should be provided");
        }
    }
}
