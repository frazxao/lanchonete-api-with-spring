package com.lanchonete.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lanchoneteOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("API de Gestão de Pedidos - Lanchonete")
                .version("1.0.0")
                .description("API REST com clientes, produtos e pedidos (itens, máquina de estados e filtros)."));
    }
}
