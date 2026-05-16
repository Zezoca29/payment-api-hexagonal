package com.hexapay.payments.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HexaPay — Payment API")
                        .description("""
                                Production-grade REST API for payment processing built with **Hexagonal Architecture** (Ports & Adapters).
                                
                                **Payment lifecycle:** `PENDING` → `COMPLETED` → `REFUNDED` | `PENDING` → `FAILED`
                                
                                All monetary values use `BigDecimal` for precision. Currency codes follow **ISO 4217** (e.g. `BRL`, `USD`).
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Kaique Augusto da Cruz Zeza")
                                .url("https://github.com/Zezoca29/payment-api-hexagonal"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local development"),
                        new Server().url("https://api.hexapay.com").description("Production")
                ));
    }
}
