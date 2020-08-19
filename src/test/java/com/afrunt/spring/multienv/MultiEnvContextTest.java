package com.afrunt.spring.multienv;

import static org.junit.jupiter.api.Assertions.*;

import com.afrunt.spring.multienv.ctx.simple.SimpleBean;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class MultiEnvContextTest {
    @Test
    public void testHappyPath() {
        MultiEnvContext multiEnvContext = new MultiEnvContext(Map.of("dev", ContextBuilder
                .annotationConfig("com.afrunt.spring.multienv.ctx.simple")
                .addMapPropertySource(Map.of("envProp", "envValue"))
                .activeProfiles("production")
        ));

        multiEnvContext.start();

        assertTrue(multiEnvContext.isStarted());

        SimpleBean simpleBean = multiEnvContext.getBean("dev", SimpleBean.class);

        assertNotNull(simpleBean);

        assertEquals("envValue", simpleBean.getEnvProp());

        multiEnvContext.close();

        assertFalse(multiEnvContext.isStarted());
    }
}
