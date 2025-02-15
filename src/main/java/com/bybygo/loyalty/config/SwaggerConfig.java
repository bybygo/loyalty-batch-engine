package com.bybygo.loyalty.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI apiInfo() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Batch Processing API")
                .description("API for managing and monitoring batch jobs")
                .version("0.0.1")
                .contact(new Contact().name("bybygo").email("bybygo@bybygo.com")));
  }
}
