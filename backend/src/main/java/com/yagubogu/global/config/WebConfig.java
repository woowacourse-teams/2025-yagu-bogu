package com.yagubogu.global.config;

import com.yagubogu.auth.interceptor.AuthInterceptor;
import com.yagubogu.auth.support.MemberClaimsArgumentResolver;
import com.yagubogu.global.LoggingInterceptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MemberClaimsArgumentResolver memberClaimsArgumentResolver;
    private final AuthInterceptor authInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(memberClaimsArgumentResolver);
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
        registry.addInterceptor(loggingInterceptor);
    }
}
