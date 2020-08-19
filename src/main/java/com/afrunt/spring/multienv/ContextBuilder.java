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
import java.util.function.Supplier;

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

    public static ContextBuilder<AnnotationConfigApplicationContext> annotationConfig(Supplier<List<String>> basePackagesSupplier, Supplier<List<Class<?>>> componentClassesSupplier) {
        return annotationConfig()
                .customizer(ctx -> {
                    List<String> basePackages = basePackagesSupplier.get();
                    List<Class<?>> componentClasses = componentClassesSupplier.get();
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
        return annotationConfig(() -> Arrays.asList(basePackages), Collections::emptyList);
    }

    public static ContextBuilder<AnnotationConfigApplicationContext> annotationConfig(Class<?>... componentClasses) {
        return annotationConfig(Collections::emptyList, () -> Arrays.asList(componentClasses));
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
        contextCustomizer = customizer;
        return this;
    }

    public ContextBuilder<T> activeProfiles(Collection<String> activeProfiles) {
        this.activeProfiles = new HashSet<>(activeProfiles);
        return this;
    }

    public ContextBuilder<T> activeProfiles(String... activeProfiles) {
        return activeProfiles(Arrays.asList(activeProfiles));
    }

    public ContextBuilder<T> defaultProfiles(Collection<String> defaultProfiles) {
        this.defaultProfiles = new HashSet<>(defaultProfiles);
        return this;
    }

    public ContextBuilder<T> defaultProfiles(String... defaultProfiles) {
        return defaultProfiles(Arrays.asList(defaultProfiles));
    }

    public ContextBuilder<T> resetPropertySource() {
        propertySource = new CompositePropertySource(randomName());
        return this;
    }

    public ContextBuilder<T> addPropertySource(PropertySource<?> propertySource) {
        this.propertySource.addFirstPropertySource(propertySource);
        return this;
    }

    public ContextBuilder<T> addMapPropertySource(Map<String, Object> source) {
        notNull(source, "source map cannot be null");
        return addPropertySource(new MapPropertySource(randomName(), new HashMap<>(source)));
    }

    public ContextBuilder<T> addPropertiesPropertySource(Properties properties) {
        addPropertySource(new PropertiesPropertySource(randomName(), properties));
        return this;
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
    }

    protected T populateEnvironmentState(T ctx) {
        ctx.getEnvironment().setActiveProfiles(activeProfiles.toArray(new String[]{}));
        ctx.getEnvironment().setDefaultProfiles(defaultProfiles.toArray(new String[]{}));
        ctx.getEnvironment().getPropertySources().addLast(propertySource);
        return ctx;
    }
}
