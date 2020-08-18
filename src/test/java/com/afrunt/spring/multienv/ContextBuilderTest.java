package com.afrunt.spring.multienv;

import com.afrunt.spring.multienv.ctx.simple.ProductionProfileBean;
import com.afrunt.spring.multienv.ctx.simple.SimpleBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.GenericApplicationContext;

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
    public void testEmptyPackagesAndClasses() {
        Assertions.assertThrows(IllegalStateException.class, () -> ContextBuilder.annotationConfig().build());
    }

    @Test
    public void testWithoutProductionProfile() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> createPackageContextBuilder()
                .activeProfiles()
                .build()
                .getBean(ProductionProfileBean.class)
        );
    }

    private ContextBuilder createPackageContextBuilder() {
        return ContextBuilder
                .annotationConfig("com.afrunt.spring.multienv.ctx.simple")
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
                .defaultProfiles();
    }

    private ContextBuilder createClassesBuilder() {
        return ContextBuilder
                .annotationConfig(SimpleBean.class, ProductionProfileBean.class)
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
                .defaultProfiles();
    }
}
