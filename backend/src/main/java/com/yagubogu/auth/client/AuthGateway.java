package com.yagubogu.auth.client;

import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.AuthResponse;

public interface AuthGateway {

    AuthResponse validateToken(final LoginRequest loginRequest);
}
