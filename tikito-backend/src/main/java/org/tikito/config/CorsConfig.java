package org.tikito.config;

import org.tikito.auth.AuthUserResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthUserResolver());
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {

        registry.addMapping("**")
                .allowedOrigins("**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}