package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.AuthResponse;

public interface AuthGateway {

    AuthResponse validateToken(final LoginRequest loginRequest);
}
