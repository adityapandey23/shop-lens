package tech.thedumbdev.shop_lens.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    SecurityConfig(final JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT APIs)
                .csrf(csrf -> csrf.disable())

                // Make session stateless (don't store user state on server)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define URL access rules
                .authorizeHttpRequests(auth -> auth
                        // Allow these paths without authentication
                        .requestMatchers(
                                "/api/v1/auth/**",      // Login, Register, Health, etc.
                                "/v3/api-docs/**",      // Swagger docs (optional)
                                "/swagger-ui/**",       // Swagger UI (optional)
                                "/swagger-ui.html"
                        ).permitAll()

                        // Require authentication for EVERYTHING else
                        .anyRequest().authenticated()
                )

                // Disable default login forms
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable());

        // Add our custom JWT filter before the standard Spring Security filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
