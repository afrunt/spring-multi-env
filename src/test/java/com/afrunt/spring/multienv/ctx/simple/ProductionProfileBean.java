package com.afrunt.spring.multienv.ctx.simple;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("production")
public class ProductionProfileBean {
}
