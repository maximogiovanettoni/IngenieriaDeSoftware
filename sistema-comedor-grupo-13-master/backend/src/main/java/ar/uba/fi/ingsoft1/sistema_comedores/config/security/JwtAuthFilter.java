package ar.uba.fi.ingsoft1.sistema_comedores.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Component
class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Autowired
    JwtAuthFilter(@org.springframework.lang.Nullable JwtService jwtService) {
        // JwtService may be absent in light-weight test slices (e.g. @WebMvcTest).
        // Accept null and make the filter a no-op in that case so tests that don't
        // provide a JwtService bean won't fail to start the context.
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getRequestURI();
        System.out.println("🔎 Incoming URI = " + uri);

        if (uri.startsWith("/api/orders/notifications/stream")) {
            System.out.println("🟢 SSE BYPASS triggered");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            this.authenticateToken(request);
            filterChain.doFilter(request, response);
        } catch (ResponseStatusException e) {
            response.sendError(e.getStatusCode().value());
        }
    }

    private void authenticateToken(HttpServletRequest request) {
        // Is the user already authenticated?
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        // Try to get the token
        String authHeader = request.getHeader("Authorization");
        String headerPrefix = "Bearer ";
        if (authHeader == null || !authHeader.startsWith(headerPrefix)) {
            return;
        }
        String token = authHeader.substring(headerPrefix.length());

        // If there is no JwtService available (e.g. in some test slices), skip
        // authentication and let other test utilities (like SecurityMockMvcRequestPostProcessors)
        // populate the SecurityContext.
        if (jwtService == null) {
            return;
        }

        jwtService.extractVerifiedUserDetails(token).ifPresent(userDetails -> {
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    List.of(new SimpleGrantedAuthority(userDetails.role().toUpperCase()))
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        });
    }
}
