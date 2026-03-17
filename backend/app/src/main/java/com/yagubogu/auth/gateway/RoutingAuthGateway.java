package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.member.domain.OAuthProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Profile("!local")
@Component
public class RoutingAuthGateway implements AuthGateway {

    private final List<ProviderAuthGateway> providerAuthGateways;

    @Override
    public AuthParam validateToken(final LoginParam loginParam) {
        OAuthProvider provider = loginParam.provider() != null ? loginParam.provider() : OAuthProvider.GOOGLE;

        return providerAuthGateways.stream()
                .filter(gateway -> gateway.supports(provider))
                .findFirst()
                .map(gateway -> gateway.validateToken(loginParam))
                .orElseThrow(() -> new UnsupportedOperationException("No auth gateway for: " + provider));
    }
}
