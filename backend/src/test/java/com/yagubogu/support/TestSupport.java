package com.yagubogu.support;

import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.member.domain.Role;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class TestSupport {

    private static final String BEARER = "Bearer ";

    public static LoginResponse loginResponse(String idToken) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(idToken))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);
    }

    public static String getAccessToken(String idToken) {
        return BEARER + loginResponse(idToken).accessToken();
    }

    public static String getAccessTokenByMemberId(long memberId, AuthTokenProvider jwtProvider) {
        Role role = getRoleByMemberId(memberId);
        MemberClaims claims = new MemberClaims(memberId, role);
        String jwt = jwtProvider.createAccessToken(claims);
        return BEARER + jwt;
    }

    private static Role getRoleByMemberId(long memberId) {
        if (memberId == 4L) {
            return Role.ADMIN;
        }
        return Role.USER;
    }
}
