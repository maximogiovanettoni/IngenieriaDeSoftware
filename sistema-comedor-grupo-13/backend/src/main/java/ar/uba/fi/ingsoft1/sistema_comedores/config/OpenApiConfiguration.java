package ar.uba.fi.ingsoft1.sistema_comedores.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import static ar.uba.fi.ingsoft1.sistema_comedores.config.security.SecurityConfig.PUBLIC_ENDPOINTS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@OpenAPIDefinition(
    info = @Info(title = "Universitary Dining Room Management System Backend")
)
@SecurityScheme(
    name = OpenApiConfiguration.BEARER_AUTH_SCHEME_KEY,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
@Component
public class OpenApiConfiguration {
    
    public static final String BEARER_AUTH_SCHEME_KEY = "Bearer Authentication";
    
    @Value("${app.base-url:http://localhost:21300}")
    private String baseUrl;
    
    @Value("${server.servlet.context-path:/api}")
    private String contextPath;
    
    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server();
        server.setUrl(baseUrl + contextPath);
        server.setDescription("API Server");
        
        return new OpenAPI()
                .servers(List.of(server));
    }
    
    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            var tags = new HashSet<String>();
            
            // Iterate over what spring calls controllers (OpenAPI paths) and paths (OpenAPI operations)
            for (var entry: openApi.getPaths().entrySet()) {
                for (var operation: entry.getValue().readOperations()) {
                    tags.addAll(operation.getTags());
                    if (Arrays.asList(PUBLIC_ENDPOINTS).contains(entry.getKey())) {
                        operation.getResponses().remove("403");
                    } else {
                        operation.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME_KEY));
                    }
                }
            }
            
            openApi.setTags(tags.stream()
                    .sorted()
                    .map(name -> new Tag().name(name))
                    .toList());
        };
    }
}