package com.yagubogu.auth.support;

import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.member.domain.OAuthProvider;

public interface AuthValidator<T extends AuthParam> {

    boolean supports(OAuthProvider provider);

    void validate(T response);
}
