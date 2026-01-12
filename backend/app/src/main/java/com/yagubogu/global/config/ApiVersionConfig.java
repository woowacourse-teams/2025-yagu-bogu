package com.yagubogu.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    public static final String API_V1 = "/api/v1";
    public static final String API_V2 = "/api/v2";

    @Bean
    public WebMvcConfigurer versioningConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configurePathMatch(final PathMatchConfigurer configurer) {
                configurer.addPathPrefix(API_V1,
                        c -> c.getPackageName().contains(".v1"));

                configurer.addPathPrefix(API_V2,
                        c -> c.getPackageName().contains(".v2"));
            }
        };
    }
}
