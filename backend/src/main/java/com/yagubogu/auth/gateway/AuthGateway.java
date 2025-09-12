package com.yagubogu.auth.gateway;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;

public interface AuthGateway {

    AuthResponse validateToken(final LoginRequest loginRequest);
}
