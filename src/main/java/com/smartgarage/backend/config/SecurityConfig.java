package com.smartgarage.backend.config;

import com.smartgarage.backend.config.JwtAuthenticationFilter;
import com.smartgarage.backend.config.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);

        http
                // dev-friendly: disable CSRF for stateless JWT usage
                .csrf(csrf -> csrf.disable())

                // stateless session (we use JWT)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // allow H2 console frames (dev only)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // authorization rules
                .authorizeHttpRequests(auth -> auth
                        // allow swagger and api docs (optional - helpful during development)
                        .requestMatchers(
                                "/v2/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-resources/**", "/swagger-ui.html",
                                "/webjars/**").permitAll()

                        // allow H2 console (dev)
                        .requestMatchers("/h2-console/**").permitAll()

                        // public auth endpoints
                        .requestMatchers("/api/auth/**", "/api/users/register").permitAll()

                        // allow preflight CORS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // booking endpoints require a valid authenticated user (explicit)
                        .requestMatchers("/api/bookings/**", "/api/booking/**").authenticated()

                        // everything else requires authentication by default
                        .anyRequest().authenticated()
                )

                // add the JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
