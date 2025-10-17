package com.yagubogu.support;

import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.v1.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class TestSupport {

    private static final String BEARER = "Bearer ";

    public static LoginResponse loginResponse(String idToken) {
        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginParam(idToken))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);
    }

    public static String getAccessTokenByMemberId(String idToken) {
        return BEARER + loginResponse(idToken).accessToken();
    }
}
