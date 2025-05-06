package org.example.wowelang_backend.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "WOWELANG API",
        description = "WOWELANG API SWAGGER",
        version = "V1")
)
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi api_File() {
        return GroupedOpenApi.builder()
                .group("와이랭 api 서버")
                .pathsToMatch("/**")
                .build();
    }
}
