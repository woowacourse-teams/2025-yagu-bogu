package com.yagubogu.auth.interceptor;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.AuthorizationExtractor;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthorizationExtractor authorizationExtractor;
    private final AuthTokenProvider jwtProvider;

    public AuthInterceptor(
            final AuthorizationExtractor authorizationExtractor,
            final AuthTokenProvider jwtProvider
    ) {
        this.authorizationExtractor = authorizationExtractor;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        if (!(handler instanceof final HandlerMethod handlerMethod)) {
            return true;
        }
        Arrays.stream(handlerMethod.getMethod().getAnnotations())
                .forEach(a -> System.out.println("method annotation: " + a.annotationType()));

        return validateToken(request, handlerMethod);
    }

    private boolean validateToken(
            final HttpServletRequest request,
            final HandlerMethod handlerMethod
    ) {
        RequireRole requireRole = findRequireRoleAnnotation(handlerMethod);
        if (requireRole != null) {
            return validateToken(request, requireRole.value());
        }

        return true;
    }

    private RequireRole findRequireRoleAnnotation(final HandlerMethod handlerMethod) {
        RequireRole methodAnnotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        RequireRole classAnnotation = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        if (classAnnotation != null) {
            return classAnnotation;
        }

        return null;
    }

    private boolean validateToken(
            final HttpServletRequest request,
            final Role role
    ) {
        String token = authorizationExtractor.extract(request)
                .orElseThrow(() -> new UnAuthorizedException("Token not exists"));

        Role actualRole = jwtProvider.getRoleByAccessToken(token);
        if (!actualRole.hasPermission(role)) {
            throw new ForbiddenException("Forbidden request");
        }

        return true;
    }
}
