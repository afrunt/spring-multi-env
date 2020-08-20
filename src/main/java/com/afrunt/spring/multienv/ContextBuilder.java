package com.afrunt.spring.multienv;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

import static org.springframework.util.Assert.notNull;

public class ContextBuilder<T extends GenericApplicationContext> {
    private final T originalInstance;
    private Set<String> activeProfiles = new HashSet<>();
    private Set<String> defaultProfiles = new HashSet<>();
    private CompositePropertySource propertySource = new CompositePropertySource("env-composite-property-source");
    private Function<T, T> contextCustomizer = ctx -> ctx;

    public ContextBuilder(T originalInstance) {
        this.originalInstance = originalInstance;
    }

    public ContextBuilder(T originalInstance, Collection<String> activeProfiles, Collection<String> defaultProfiles, CompositePropertySource propertySource, Function<T, T> contextCustomizer) {
        this(originalInstance);
        this.activeProfiles = new HashSet<>(activeProfiles);
        this.defaultProfiles = new HashSet<>(defaultProfiles);
        CompositePropertySource compositePropertySource = new CompositePropertySource("env-composite-property-source");
        compositePropertySource.addFirstPropertySource(propertySource);
        this.propertySource = compositePropertySource;
        this.contextCustomizer = contextCustomizer;
    }

    public static ContextBuilder<AnnotationConfigApplicationContext> annotationConfig(List<String> basePackages, List<Class<?>> componentClasses) {
        return annotationConfig()
                .customizer(ctx -> {
                    if (!basePackages.isEmpty()) {
                        ctx.scan(basePackages.toArray(new String[]{}));
                    }
                    if (!componentClasses.isEmpty()) {
                        ctx.register(componentClasses.toArray(new Class[]{}));
                    }
                    return ctx;
                });
    }

    public static ContextBuilder<AnnotationConfigApplicationContext> annotationConfig(String... basePackages) {
        return annotationConfig(Arrays.asList(basePackages), Collections.emptyList());
    }

    public static ContextBuilder<AnnotationConfigApplicationContext> annotationConfig(Class<?>... componentClasses) {
        return annotationConfig(Collections.emptyList(), Arrays.asList(componentClasses));
    }

    private static ContextBuilder<AnnotationConfigApplicationContext> annotationConfig() {
        return new ContextBuilder<>(new AnnotationConfigApplicationContext());
    }

    public GenericApplicationContext build() {
        validateConfiguration();

        T ctx = populateEnvironmentState(originalInstance);
        ctx = contextCustomizer.apply(ctx);

        ctx.refresh();

        return ctx;
    }

    public ContextBuilder<T> customizer(Function<T, T> customizer) {
        return new ContextBuilder<>(originalInstance, activeProfiles, defaultProfiles, propertySource, customizer);
    }

    public ContextBuilder<T> activeProfiles(Collection<String> activeProfiles) {
        return new ContextBuilder<>(originalInstance, activeProfiles, defaultProfiles, propertySource, contextCustomizer);
    }

    public ContextBuilder<T> activeProfiles(String... activeProfiles) {
        return activeProfiles(Arrays.asList(activeProfiles));
    }

    public ContextBuilder<T> defaultProfiles(Collection<String> defaultProfiles) {
        return new ContextBuilder<>(originalInstance, activeProfiles, defaultProfiles, propertySource, contextCustomizer);
    }

    public ContextBuilder<T> defaultProfiles(String... defaultProfiles) {
        return defaultProfiles(Arrays.asList(defaultProfiles));
    }

    public ContextBuilder<T> resetPropertySource() {
        return new ContextBuilder<>(originalInstance, activeProfiles, defaultProfiles, new CompositePropertySource(randomName()), contextCustomizer);
    }

    public ContextBuilder<T> addPropertySource(PropertySource<?> propertySource) {
        CompositePropertySource compositePropertySource = new CompositePropertySource(randomName());
        compositePropertySource.addFirstPropertySource(this.propertySource);
        compositePropertySource.addFirstPropertySource(propertySource);

        return new ContextBuilder<>(originalInstance, activeProfiles, defaultProfiles, compositePropertySource, contextCustomizer);
    }

    public ContextBuilder<T> addMapPropertySource(Map<String, Object> source) {
        notNull(source, "source map cannot be null");
        return addPropertySource(new MapPropertySource(randomName(), new HashMap<>(source)));
    }

    public ContextBuilder<T> addPropertiesPropertySource(Properties properties) {
        return addPropertySource(new PropertiesPropertySource(randomName(), properties));
    }

    public ContextBuilder<T> addPropertiesPropertySource(InputStream is) {
        notNull(is, "properties input stream cannot be null");
        try {
            Properties properties = new Properties();
            properties.load(is);
            return addPropertiesPropertySource(properties);
        } catch (IOException e) {
            throw new RuntimeException("Error reading properties from input stream", e);
        }
    }

    protected String randomName() {
        return UUID.randomUUID().toString();
    }

    protected void validateConfiguration() {
        notNull(propertySource, "propertySource cannot be null");
        notNull(activeProfiles, "activeProfiles cannot be null");
        notNull(defaultProfiles, "defaultProfiles cannot be null");
        notNull(contextCustomizer, "contextCustomizer cannot be null");
    }

    protected T populateEnvironmentState(T ctx) {
        ctx.getEnvironment().setActiveProfiles(activeProfiles.toArray(new String[]{}));
        ctx.getEnvironment().setDefaultProfiles(defaultProfiles.toArray(new String[]{}));
        ctx.getEnvironment().getPropertySources().addLast(propertySource);
        return ctx;
    }
}
