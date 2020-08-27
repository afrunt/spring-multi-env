package com.afrunt.spring.multienv;

import com.afrunt.spring.multienv.ctx.simple.SimpleBean;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MultiEnvContextTest {
    @Test
    public void testHappyPath() {
        MultiEnvContext multiEnvContext = new MultiEnvContext(Map.of("dev", ContextBuilder
                .annotationConfig("com.afrunt.spring.multienv.ctx.simple")
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
        ));

        multiEnvContext.start();

        assertIterableEquals(List.of("dev"), multiEnvContext.environmentNames());
        assertTrue(multiEnvContext.isStarted());

        SimpleBean simpleBean = multiEnvContext.getBean("dev", SimpleBean.class);

        assertNotNull(simpleBean);

        assertEquals("envValue", simpleBean.getEnvProp());

        multiEnvContext.close();

        assertFalse(multiEnvContext.isStarted());
    }
}
