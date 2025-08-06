package com.yagubogu.fixture;

import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class TestSupport {

    private static final String BEARER = "Bearer ";

    public static LoginResponse loginResponse(String idToken) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(idToken))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().as(LoginResponse.class);
    }

    public static String getAccessToken(String idToken) {
        return BEARER + loginResponse(idToken).accessToken();
    }

    public static String getRefreshToken(String idToken) {
        return loginResponse(idToken).refreshToken();
    }
}
