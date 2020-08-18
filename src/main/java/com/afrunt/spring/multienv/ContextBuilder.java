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
import java.util.stream.Collectors;

import static org.springframework.util.Assert.noNullElements;
import static org.springframework.util.Assert.notNull;

public abstract class ContextBuilder {
    protected Set<String> activeProfiles = new HashSet<>();
    protected Set<String> defaultProfiles = new HashSet<>();
    protected CompositePropertySource propertySource = new CompositePropertySource("env-composite-property-source");

    public static AnnotationConfigContextBuilder annotationConfig() {
        return new AnnotationConfigContextBuilder();
    }

    public static AnnotationConfigContextBuilder annotationConfig(String... basePackages) {
        return annotationConfig()
                .basePackages(basePackages);
    }

    public static AnnotationConfigContextBuilder annotationConfig(Class<?>... componentClasses) {
        return annotationConfig()
                .componentClasses(componentClasses);
    }

    public abstract GenericApplicationContext build();

    public ContextBuilder activeProfiles(Collection<String> activeProfiles) {
        this.activeProfiles = new HashSet<>(activeProfiles);
        return this;
    }

    public ContextBuilder activeProfiles(String... activeProfiles) {
        return activeProfiles(Arrays.asList(activeProfiles));
    }

    public ContextBuilder defaultProfiles(Collection<String> defaultProfiles) {
        this.defaultProfiles = new HashSet<>(defaultProfiles);
        return this;
    }

    public ContextBuilder defaultProfiles(String... defaultProfiles) {
        return defaultProfiles(Arrays.asList(defaultProfiles));
    }

    public ContextBuilder resetPropertySource() {
        propertySource = new CompositePropertySource(randomName());
        return this;
    }

    public ContextBuilder addPropertySource(PropertySource<?> propertySource) {
        this.propertySource.addFirstPropertySource(propertySource);
        return this;
    }

    public ContextBuilder addMapPropertySource(Map<String, Object> source) {
        notNull(source, "source map cannot be null");
        return addPropertySource(new MapPropertySource(randomName(), new HashMap<>(source)));
    }

    public ContextBuilder addPropertiesPropertySource(Properties properties) {
        addPropertySource(new PropertiesPropertySource(randomName(), properties));
        return this;
    }

    public ContextBuilder addPropertiesPropertySource(InputStream is) {
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

    @SuppressWarnings("unchecked")
    public <T> T cast() {
        return (T) this;
    }

    protected void validateConfiguration() {
        notNull(propertySource, "propertySource cannot be null");
        notNull(activeProfiles, "activeProfiles cannot be null");
        notNull(defaultProfiles, "defaultProfiles cannot be null");
    }

    protected <T extends GenericApplicationContext> T populateEnvironmentState(T ctx) {
        ctx.getEnvironment().setActiveProfiles(activeProfiles.toArray(new String[]{}));
        ctx.getEnvironment().setDefaultProfiles(defaultProfiles.toArray(new String[]{}));
        ctx.getEnvironment().getPropertySources().addLast(propertySource);
        return ctx;
    }

    public static class AnnotationConfigContextBuilder extends ContextBuilder {
        private List<String> basePackages = new ArrayList<>();
        private List<Class<?>> componentClasses = new ArrayList<>();

        @Override
        public AnnotationConfigApplicationContext build() {
            validateConfiguration();
            AnnotationConfigApplicationContext ctx = populateEnvironmentState(new AnnotationConfigApplicationContext());

            if (!basePackages.isEmpty()) {
                ctx.scan(basePackages.toArray(new String[]{}));
            }

            if (!componentClasses.isEmpty()) {
                ctx.register(componentClasses.toArray(new Class[]{}));
            }

            ctx.refresh();

            return ctx;
        }

        public AnnotationConfigContextBuilder basePackages(String... basePackages) {
            notNull(basePackages, "basePackages cannot be null");
            noNullElements(basePackages, "basePackage cannot be null");

            this.basePackages = Arrays.stream(basePackages)
                    .collect(Collectors.toList());

            return this;
        }

        public AnnotationConfigContextBuilder componentClasses(Class<?>... componentClasses) {
            notNull(componentClasses, "componentClasses cannot be null");
            noNullElements(componentClasses, "componentClass cannot be null");

            this.componentClasses = Arrays.stream(componentClasses)
                    .collect(Collectors.toList());

            return this;
        }

        @Override
        protected void validateConfiguration() {
            super.validateConfiguration();
            notNull(basePackages, "basePackages cannot be null");
            notNull(componentClasses, "componentClasses cannot be null");

            if (basePackages.isEmpty() && componentClasses.isEmpty()) {
                throw new IllegalStateException("Either basePackages or componentClasses should be provided");
            }
        }
    }
}
