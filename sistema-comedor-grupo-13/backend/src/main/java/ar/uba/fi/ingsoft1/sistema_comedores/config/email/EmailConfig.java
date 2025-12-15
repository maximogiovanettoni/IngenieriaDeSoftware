package ar.uba.fi.ingsoft1.sistema_comedores.config.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    
    @Value("${app.base-url:http://localhost:21300}")
    private String baseUrl;
    
    public String getVerificationUrl(String token) {
        return baseUrl + "/verify-email?token=" + token;
    }

    public String getResetPasswordUrl(String token) {
        return baseUrl + "/reset?token=" + token;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}