package com.warmplace.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI warmplaceMessengerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Warmplace Messenger API")
                        .description("카프카 기반 메신저 서비스 API 문서입니다.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Warmplace Team")
                                .email("dev@warmplace.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
