package ar.uba.fi.ingsoft1.sistema_comedores.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity(debug = false)
public class SecurityConfig {

    public static final String[] PUBLIC_ENDPOINTS = {
        "/users", 
        "/sessions", 
        "/auth/verify-email", 
        "/auth/resend-verification", 
        "/auth/verification-status",
        "/config/validation",
        "/password/reset-request",
        "/password/reset"
    };

    private final JwtAuthFilter authFilter;
    
    @Value("${app.external-url:http://localhost:21300}")
    private String externalUrl;

    @Autowired
    SecurityConfig(JwtAuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new RestSecurityHandlers.JsonAuthenticationEntryPoint())
                .accessDeniedHandler(new RestSecurityHandlers.JsonAccessDeniedHandler())
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/products").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/products/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/products").permitAll()
                .requestMatchers(HttpMethod.POST, "/ingredients").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/ingredients/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/ingredients/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/ingredients/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/ingredients/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/ingredients").permitAll()
                .requestMatchers(HttpMethod.POST, "/combos").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/combos/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/combos/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/combos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/combos").permitAll()
                .requestMatchers(HttpMethod.GET, "/menu").permitAll()
                .requestMatchers(HttpMethod.POST, "/orders").authenticated()
                .requestMatchers("/orders/notifications/stream").permitAll()
                .requestMatchers("/orders/notifications/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/orders/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/orders/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/orders/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/orders/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/staff/orders").authenticated()
                .requestMatchers(HttpMethod.GET, "/staff/orders/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/staff/orders/**").authenticated()
                .requestMatchers("/profile/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/promotions/active").permitAll()
                .requestMatchers(HttpMethod.GET, "/promotions/valid").permitAll()
                .requestMatchers(HttpMethod.GET, "/promotions/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/promotions/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/promotions/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/promotions/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/promotions/**").hasAnyAuthority("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/promotions/**").hasAnyAuthority("ADMIN")
                .anyRequest().denyAll())
            .sessionManagement(sessionManager -> sessionManager
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Build allowed origins list - support both development and production
        List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
            "http://localhost:21300",
            "http://127.0.0.1:21300",
            "https://grupo-13.tp1.ingsoft1.fiuba.ar"
        ));
        
        // Also add the external URL if it's different and configured
        if (externalUrl != null && !externalUrl.isEmpty() && 
            !allowedOrigins.contains(externalUrl)) {
            String origin = externalUrl;
            if (origin.endsWith("/")) {
                origin = origin.substring(0, origin.length() - 1);
            }
            allowedOrigins.add(origin);
        }
        
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}