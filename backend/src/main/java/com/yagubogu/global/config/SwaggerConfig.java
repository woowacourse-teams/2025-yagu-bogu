package com.yagubogu.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(SwaggerUiServersProperties.class)
@Configuration
public class SwaggerConfig {


    private final SwaggerUiServersProperties serversProps;

    public SwaggerConfig(SwaggerUiServersProperties serversProps) {
        this.serversProps = serversProps;
    }

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI()
                .components(new Components().addSecuritySchemes("Access Token",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("Access Token"))
                .info(apiInfo());

        // application.yml의 springdoc.swagger-ui.servers를 OpenAPI servers에 반영
        if (serversProps.getServers() != null && !serversProps.getServers().isEmpty()) {
            openAPI.setServers(
                    serversProps.getServers().stream()
                            .map(s -> new Server().url(s.getUrl()).description(s.getName()))
                            .collect(Collectors.toList())
            );
        }

        return openAPI;
    }

    private Info apiInfo() {
        return new Info()
                .title("YaguBogu API")
                .description("야구보구 API 명세입니다.")
                .version("1.0.0");
    }
}
