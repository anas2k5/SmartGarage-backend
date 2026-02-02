package com.smartgarage.backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
    }

    // ================= AUTH MANAGER =================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration cfg
    ) throws Exception {
        return cfg.getAuthenticationManager();
    }

    // ================= AUTH PROVIDER =================
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            PasswordEncoder encoder,
            UserDetailsService uds
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    // ================= SECURITY FILTER =================
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtUtils, userDetailsService);

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(headers ->
                        headers.frameOptions(frame -> frame.disable())
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint())
                        .accessDeniedHandler(restAccessDeniedHandler())
                )
                .authorizeHttpRequests(auth -> auth

                        // ================= PUBLIC =================
                        .requestMatchers(HttpMethod.POST,
                                "/api/payments/stripe/webhook"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/**",
                                "/api/users/register"
                        ).permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // ================= ADMIN =================
                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ADMIN")

                        // ================= OWNER =================
                        .requestMatchers("/api/garages/me")
                        .hasAnyAuthority("OWNER", "ADMIN")

                        // ================= PAYMENTS =================
                        .requestMatchers("/api/payments/me")
                        .hasAnyAuthority("CUSTOMER", "ADMIN")

                        .requestMatchers("/api/payments/initiate/**")
                        .hasAuthority("CUSTOMER")

                        .requestMatchers("/api/payments/status/**")
                        .hasAnyAuthority("CUSTOMER", "OWNER", "ADMIN")

                        .requestMatchers("/api/payments/invoice/**")
                        .hasAnyAuthority("CUSTOMER", "OWNER", "ADMIN")

                        // ================= JOB CARDS =================
                        .requestMatchers("/api/jobcards/me")
                        .hasAnyAuthority("MECHANIC", "ADMIN")

                        .requestMatchers("/api/jobcards/**")
                        .hasAnyAuthority("MECHANIC", "OWNER", "ADMIN")

                        // ================= CORE APP =================
                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/bookings/**",
                                "/api/vehicles/**",
                                "/api/garages/**",
                                "/api/invoices/**",
                                "/api/mechanics/**"
                        ).authenticated()

                        // ================= FALLBACK =================
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(
                        daoAuthenticationProvider(
                                passwordEncoder(),
                                userDetailsService
                        )
                );

        return http.build();
    }

    // ================= CORS =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ================= AUTH ERRORS =================
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.core.AuthenticationException ex) -> {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\"" +
                            ex.getMessage() + "\"}"
            );
        };
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException ex) -> {

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(
                    "{\"error\":\"Forbidden\",\"message\":\"" +
                            ex.getMessage() + "\"}"
            );
        };
    }

    // ================= PASSWORD =================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
