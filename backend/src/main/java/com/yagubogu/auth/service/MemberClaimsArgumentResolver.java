package com.yagubogu.auth.service;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.global.exception.UnAuthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
@Component
public class MemberClaimsArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthorizationExtractor authorizationExtractor;
    private final AuthService authService;

    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().equals(MemberClaims.class);
    }

    @Override
    public Object resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) throws Exception {
        return authorizationExtractor.extract(webRequest)
                .map(authService::makeMemberClaims)
                .orElseThrow((() -> new UnAuthorizedException("Access token not found")));
    }
}
