package com.afrunt.spring.multienv;

import com.afrunt.spring.multienv.ctx.simple.ProductionProfileBean;
import com.afrunt.spring.multienv.ctx.simple.SimpleBean;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ContextBuilderTest {
    @Test
    public void testHappyPath() {
        ContextBuilder packageBuilder = ContextBuilder
                .annotationConfig("com.afrunt.spring.multienv.ctx.simple")
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
                .defaultProfiles();

        ContextBuilder classesBuilder = ContextBuilder
                .annotationConfig(SimpleBean.class, ProductionProfileBean.class)
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
                .defaultProfiles();

        Consumer<GenericApplicationContext> happyPathConsumer = ctx -> {
            SimpleBean simpleBean = ctx.getBean(SimpleBean.class);
            assertNotNull(simpleBean);
            assertEquals("envValue", simpleBean.getEnvProp());
            assertNotNull(ctx.getBean(ProductionProfileBean.class));
        };

        happyPathConsumer.accept(packageBuilder.build());
        happyPathConsumer.accept(classesBuilder.build());
    }

    @Test
    public void testPropertySourcePriority(){
        GenericApplicationContext ctx = ContextBuilder
                .annotationConfig("com.afrunt.spring.multienv.ctx.simple")
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .addMapPropertySource(Map.of("envProp", "higherPriorityValue"))
                .build();

        assertEquals("higherPriorityValue", ctx.getBean(SimpleBean.class).getEnvProp());
    }

    @Test
    public void testEmptyPackagesAndClasses(){
        Assertions.assertThrows(IllegalStateException.class, () -> ContextBuilder.annotationConfig().build());
    }
}
