package com.yagubogu.fixture;

import static com.yagubogu.auth.service.AuthorizationExtractor.BEARER_PREFIX;

import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

public class TestSupport {

    public static LoginResponse loginResponse(String idToken) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(idToken))
                .when().post("/api/auth/login")
                .then().statusCode(200)
                .extract().as(LoginResponse.class);
    }

    public static String getAccessToken(String idToken) {
        return BEARER_PREFIX + loginResponse(idToken).accessToken();
    }
}
