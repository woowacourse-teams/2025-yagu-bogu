package com.yagubogu.global.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "springdoc.swagger-ui")
public class SwaggerUiServersProperties {

    private List<ServerEntry> servers = new ArrayList<>();

    public List<ServerEntry> getServers() {
        return servers;
    }

    public void setServers(List<ServerEntry> servers) {
        this.servers = servers;
    }

    public static class ServerEntry {
        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
