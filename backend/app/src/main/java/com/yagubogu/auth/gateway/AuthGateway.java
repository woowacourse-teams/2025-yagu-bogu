package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.AuthParam;

public interface AuthGateway {

    AuthParam validateToken(final LoginParam loginParam);
}
