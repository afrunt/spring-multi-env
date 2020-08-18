package com.afrunt.spring.multienv;

import com.afrunt.spring.multienv.ctx.simple.SimpleBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.MapPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AnnotationConfigContextInitializerTest {
    @Test
    public void testNullParams() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AnnotationConfigContextInitializer().basePackages(null, null)
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AnnotationConfigContextInitializer().componentClasses(null, null)
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AnnotationConfigContextInitializer().basePackages("", null, "")
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AnnotationConfigContextInitializer().componentClasses(Integer.class, null, Long.class)
        );

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new AnnotationConfigContextInitializer().initialize(null)
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> new AnnotationConfigContextInitializer()
                .initialize(new StubEnvironmentConfiguration())
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> new AnnotationConfigContextInitializer()
                .initialize(new StubEnvironmentConfiguration("", null))
        );

        Assertions.assertThrows(IllegalArgumentException.class, () -> new AnnotationConfigContextInitializer()
                .initialize(new StubEnvironmentConfiguration("dev", null))
        );

        Assertions.assertThrows(IllegalStateException.class, () -> new AnnotationConfigContextInitializer()
                .initialize(new StubEnvironmentConfiguration("dev", new CompositePropertySource("propertySource")))
        );
    }

    @Test
    public void testSimpleContextConfig() {
        AnnotationConfigContextInitializer initializer = new AnnotationConfigContextInitializer() {
            @Override
            public List<String> getActiveProfiles() {
                return List.of("test");
            }
        };

        CompositePropertySource propertySource = new CompositePropertySource("propertySource");
        propertySource.addFirstPropertySource(new MapPropertySource("map", Map.of("envProp", "envValue")));
        ApplicationContext ctx = initializer
                .basePackages("com.afrunt.spring.multienv")
                .componentClasses(SimpleBean.class)
                .initialize(new StubEnvironmentConfiguration("dev", propertySource));

        Assertions.assertIterableEquals(List.of("test"), Arrays.asList(ctx.getEnvironment().getActiveProfiles()));
        Assertions.assertEquals("envValue", ctx.getEnvironment().getProperty("envProp"));
        SimpleBean simpleBean = ctx.getBean(SimpleBean.class);
        Assertions.assertEquals("envValue", simpleBean.getEnvProp());
    }

    private static class StubEnvironmentConfiguration implements EnvironmentConfiguration {

        private String environmentId;
        private CompositePropertySource propertySource;

        public StubEnvironmentConfiguration() {
        }


        public StubEnvironmentConfiguration(String environmentId, CompositePropertySource propertySource) {
            this.environmentId = environmentId;
            this.propertySource = propertySource;
        }

        @Override
        public String getEnvironmentId() {
            return environmentId;
        }

        @Override
        public CompositePropertySource getPropertySource() {
            return propertySource;
        }

    }
}
