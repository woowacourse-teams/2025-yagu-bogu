package com.yagubogu.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "oauth.google")
public class GoogleAuthProperties {

    private String baseUri;
    private String tokenInfoUri;
    private String clientId;
}
