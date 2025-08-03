package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.Member;

public interface AuthResponse {

    Member toMember();

    String oauthId();
}
