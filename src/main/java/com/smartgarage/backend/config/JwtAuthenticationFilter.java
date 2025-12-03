package com.smartgarage.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, CustomUserDetailsService uds) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = uds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain fc)
            throws ServletException, IOException {
        try {
            String header = req.getHeader("Authorization");
            log.debug("Incoming request {} {} AuthorizationHeader={}", req.getMethod(), req.getRequestURI(), header == null ? "null" : (header.length() > 30 ? header.substring(0,30) + "..." : header));

            String token = (StringUtils.hasText(header) && header.startsWith("Bearer ")) ? header.substring(7) : null;
            if (token != null) {
                boolean valid = jwtUtils.validateToken(token);
                log.debug("JWT present — validateToken = {}", valid);
                if (valid) {
                    String username = jwtUtils.getUsernameFromToken(token);
                    log.debug("Token username: {}", username);

                    UserDetails ud = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    Authentication after = SecurityContextHolder.getContext().getAuthentication();
                    log.debug("Authentication set in SecurityContext: principal={} authenticated={} authorities={}",
                            after == null ? "null" : after.getName(),
                            after == null ? "false" : after.isAuthenticated(),
                            after == null ? "null" : after.getAuthorities());
                } else {
                    log.debug("JWT is invalid or expired");
                }
            } else {
                log.debug("No Bearer token found in Authorization header");
            }
        } catch (Exception ex) {
            log.error("Exception in JwtAuthenticationFilter doFilterInternal", ex);
            // do not stop filter chain; allow downstream security to handle unauthorized request
        }

        fc.doFilter(req, res);
    }
}
