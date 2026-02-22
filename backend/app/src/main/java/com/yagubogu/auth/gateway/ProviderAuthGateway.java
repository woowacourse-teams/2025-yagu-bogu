package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.member.domain.OAuthProvider;

public interface ProviderAuthGateway {

    boolean supports(OAuthProvider provider);

    AuthParam validateToken(final LoginParam loginParam);
}
