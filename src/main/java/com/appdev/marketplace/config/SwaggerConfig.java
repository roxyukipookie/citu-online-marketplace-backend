package com.appdev.marketplace.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Marketplace API")
                        .description("API documentation for the CampusCart Online Marketplace System.\n\n"
                                + "**Contacts:**\n"
                                + "- Karen Lean Kay Cabarrubias (karenleankay.cabarrubias.cit@edu)\n"
                                + "- Chrizza Arnie T. Gales (chrizzaarnie.gales@cit.edu)\n"
                                + "- Kiana Marquisa Del Mar (kianamarquisa.delmar@cit.edu)\n)"
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Karen Lean Kay Cabarrubias")
                                .email("karenleankay.cabarrubias.cit@edu")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")
                        )
                );
    }
}

