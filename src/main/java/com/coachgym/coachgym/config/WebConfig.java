package com.coachgym.coachgym.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RolInterceptor rolInterceptor;

    public WebConfig(RolInterceptor rolInterceptor) {
        this.rolInterceptor = rolInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rolInterceptor).addPathPatterns("/**");
    }
}