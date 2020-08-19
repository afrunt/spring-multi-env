package com.afrunt.spring.multienv;

import com.afrunt.spring.multienv.ctx.simple.ProductionProfileBean;
import com.afrunt.spring.multienv.ctx.simple.SimpleBean;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ContextBuilderTest {
    @Test
    public void testHappyPath() {
        Stream.of(createPackageContextBuilder(), createClassesBuilder())
                .map(ContextBuilder::build)
                .forEach(ctx -> {
                    SimpleBean simpleBean = ctx.getBean(SimpleBean.class);
                    assertNotNull(simpleBean);
                    assertEquals("envValue", simpleBean.getEnvProp());
                    assertNotNull(ctx.getBean(ProductionProfileBean.class));
                });
    }

    @Test
    public void testPropertySourcePriority() {
        GenericApplicationContext ctx = createPackageContextBuilder()
                .addMapPropertySource(Map.of("envProp", "higherPriorityValue"))
                .build();

        assertEquals("higherPriorityValue", ctx.getBean(SimpleBean.class).getEnvProp());
    }

    @Test
    public void testWithoutProductionProfile() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> createPackageContextBuilder()
                .activeProfiles()
                .build()
                .getBean(ProductionProfileBean.class)
        );
    }

    @Test
    public void testPropertiesFromFromFile() {
        GenericApplicationContext ctx = createPackageContextBuilder()
                .resetPropertySource()
                .addPropertiesPropertySource(getClass().getClassLoader().getResourceAsStream("env-0.properties"))
                .addPropertiesPropertySource(getClass().getClassLoader().getResourceAsStream("env-1.properties"))
                .build();

        assertEquals("higherPriorityValue", ctx.getBean(SimpleBean.class).getEnvProp());
    }

    @Test
    public void testFailedPropertiesFromFromFile() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("env-0.properties");
        assertNotNull(is);
        is.close();
        assertThrows(RuntimeException.class, () -> createPackageContextBuilder()
                .addPropertiesPropertySource(is)
        );
    }

    private ContextBuilder<AnnotationConfigApplicationContext> createPackageContextBuilder() {
        return ContextBuilder
                .annotationConfig("com.afrunt.spring.multienv.ctx.simple")
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
                .defaultProfiles();
    }

    private ContextBuilder<AnnotationConfigApplicationContext> createClassesBuilder() {
        return ContextBuilder
                .annotationConfig(SimpleBean.class, ProductionProfileBean.class)
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
                .defaultProfiles();
    }
}
