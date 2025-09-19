package com.yagubogu.auth.dto;

import com.yagubogu.member.domain.Member;

public interface AuthResponse {

    String oauthId();

    String email();

    String picture();

    Member toMember();
}
