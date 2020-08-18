package com.afrunt.spring.multienv;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ContextBuilder {
    protected Set<String> activeProfiles = new HashSet<>();
    protected Set<String> defaultProfiles = new HashSet<>();
    protected CompositePropertySource propertySource = new CompositePropertySource("env-composite-property-source");

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

    public ContextBuilder addPropertySource(PropertySource<?> propertySource) {
        this.propertySource.addFirstPropertySource(propertySource);
        return this;
    }

    public ContextBuilder addMapPropertySource(Map<String, Object> source) {
        Assert.notNull(source, "source map cannot be null");
        return addPropertySource(new MapPropertySource(UUID.randomUUID().toString(), new HashMap<>(source)));
    }

/*    @SuppressWarnings("unchecked")
    public <T> T cast() {
        return (T) this;
    }*/

    protected void validateConfiguration() {
        Assert.notNull(propertySource, "propertySource cannot be null");
        Assert.notNull(activeProfiles, "activeProfiles cannot be null");
        Assert.notNull(defaultProfiles, "defaultProfiles cannot be null");
    }

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

    public static class AnnotationConfigContextBuilder extends ContextBuilder {
        private List<String> basePackages = new ArrayList<>();
        private List<Class<?>> componentClasses = new ArrayList<>();

        @Override
        public GenericApplicationContext build() {
            validateConfiguration();
            AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
            ctx.getEnvironment().setActiveProfiles(activeProfiles.toArray(new String[]{}));
            ctx.getEnvironment().setDefaultProfiles(defaultProfiles.toArray(new String[]{}));
            ctx.getEnvironment().getPropertySources().addLast(propertySource);

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
            Assert.notNull(basePackages, "basePackages cannot be null");
            Assert.noNullElements(basePackages, "basePackage cannot be null");

            this.basePackages = Arrays.stream(basePackages)
                    .collect(Collectors.toList());
            return this;
        }

        public AnnotationConfigContextBuilder componentClasses(Class<?>... componentClasses) {
            Assert.notNull(componentClasses, "componentClasses cannot be null");
            Assert.noNullElements(componentClasses, "componentClass cannot be null");

            this.componentClasses = Arrays.stream(componentClasses)
                    .collect(Collectors.toList());

            return this;
        }

        @Override
        protected void validateConfiguration() {
            super.validateConfiguration();
            Assert.notNull(basePackages, "basePackages cannot be null");
            Assert.notNull(componentClasses, "componentClasses cannot be null");

            if (basePackages.isEmpty() && componentClasses.isEmpty()) {
                throw new IllegalStateException("Either basePackages or componentClasses should be provided");
            }
        }
    }
}
