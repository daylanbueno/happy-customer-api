package com.github.daylanbueno.happycustomer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.github.daylanbueno.happycustomer.resource"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private Contact contact() {
        return new Contact(
                "Dailan Bueno",
                "https://github.com/daylanbueno",
                "daylansantos@gmail.com"
        );
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Happy Customer API")
                .description("API that calculates and returns the reward points")
                .version("1.0")
                .contact(contact())
                .build();
    }
}