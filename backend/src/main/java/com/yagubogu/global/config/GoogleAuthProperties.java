package com.yagubogu.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public class GoogleAuthProperties {

    private String baseUri;
    private String tokenInfoUri;
    private String clientId;

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(final String baseUri) {
        this.baseUri = baseUri;
    }

    public String getTokenInfoUri() {
        return tokenInfoUri;
    }

    public void setTokenInfoUri(final String tokenInfoUri) {
        this.tokenInfoUri = tokenInfoUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }
}
