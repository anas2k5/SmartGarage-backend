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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    public SecurityConfig(
            CustomUserDetailsService userDetailsService,
            JwtUtils jwtUtils
    ) {
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
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();

        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);

        return provider;
    }

    // ================= MAIN SECURITY FILTER =================
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtUtils,
                        userDetailsService
                );

        http
                // Disable CSRF (JWT-based API)
                .csrf(csrf -> csrf.disable())

                // Enable CORS
                .cors(cors -> {})

                // Stateless session
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Allow H2 Console Frames
                .headers(headers ->
                        headers.frameOptions(
                                frame -> frame.disable()
                        )
                )

                // Exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(
                                restAuthenticationEntryPoint()
                        )
                        .accessDeniedHandler(
                                restAccessDeniedHandler()
                        )
                )

                // ================= AUTHORIZATION RULES =================
                .authorizeHttpRequests(auth -> auth

                        // ================= STRIPE WEBHOOK (PUBLIC, MUST BE FIRST) =================
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/payments/stripe/webhook"
                        ).permitAll()

                        // ================= SWAGGER =================
                        .requestMatchers(
                                "/v2/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**"
                        ).permitAll()

                        // ================= H2 =================
                        .requestMatchers("/h2-console/**")
                        .permitAll()

                        // ================= AUTH =================
                        .requestMatchers("/api/auth/**")
                        .permitAll()
                        .requestMatchers("/api/users/register")
                        .permitAll()

                        // ================= OPTIONS =================
                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        ).permitAll()

                        // ================= PROTECTED APIs =================
                        .requestMatchers(
                                "/api/vehicles/**",
                                "/api/bookings/**",
                                "/api/garages/**",
                                "/api/payments/**",
                                "/api/invoices/**",
                                "/api/dashboard/**"
                        ).authenticated()

                        // ================= FALLBACK =================
                        .anyRequest()
                        .authenticated()
                )

                // Add JWT filter
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // Auth provider
                .authenticationProvider(
                        daoAuthenticationProvider(
                                passwordEncoder(),
                                userDetailsService
                        )
                );

        return http.build();
    }

    // ================= CORS CONFIG =================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config =
                new CorsConfiguration();

        config.setAllowedOriginPatterns(
                List.of("*")
        );
        config.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                )
        );
        config.setAllowedHeaders(
                List.of("*")
        );
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                config
        );

        return source;
    }

    // ================= AUTH ERRORS =================
    @Bean
    public AuthenticationEntryPoint
    restAuthenticationEntryPoint() {
        return (
                HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.core.AuthenticationException ex
        ) -> {
            response.setContentType("application/json");
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            response.getWriter().write(
                    "{\"error\":\"Unauthorized\",\"message\":\""
                            + ex.getMessage()
                            + "\"}"
            );
        };
    }

    @Bean
    public AccessDeniedHandler
    restAccessDeniedHandler() {
        return (
                HttpServletRequest request,
                HttpServletResponse response,
                org.springframework.security.access.AccessDeniedException ex
        ) -> {
            response.setContentType("application/json");
            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );
            response.getWriter().write(
                    "{\"error\":\"Forbidden\",\"message\":\""
                            + ex.getMessage()
                            + "\"}"
            );
        };
    }

    // ================= PASSWORD =================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
