package com.software.upskilled.config;


import com.software.upskilled.service.UserService;
import com.software.upskilled.utils.JWTUtil;
import com.software.upskilled.utils.TokenBlackListService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService myUserDetailsService;

    @Autowired
    private TokenBlackListService blacklistService;


    /**
     * Performs internal filtering of HTTP requests to extract and validate JWT tokens,
     * and sets up the SecurityContext with the authenticated user details if valid.
     *
     * @param request the HTTP request to be processed.
     * @param response the HTTP response to be modified.
     * @param chain the filter chain to pass the request and response to the next filter.
     * @throws ServletException if the request processing encounters an error.
     * @throws IOException if there is an input/output error while processing the request.
     *
     * This filter checks for the presence of a Bearer token in the Authorization header,
     * validates the token, and sets up the authentication context if the token is valid and
     * the user is not already authenticated. If the token is blacklisted, the response will
     * be set to unauthorized. Otherwise, the filter proceeds to the next filter in the chain.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // Extract the Authorization header from the request
        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extract the JWT token by removing the "Bearer " prefix
            jwt = authorizationHeader.substring(7);

            // Extract the email/username from the JWT using a utility method
            email = jwtUtil.extractUsername(jwt);

            // Check if the token is blacklisted (e.g., manually logged out or invalidated)
            if (blacklistService.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

        }
        // If an email/username is extracted and no authentication is set in the SecurityContext
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.myUserDetailsService.loadUserByUsername(email);
            // Validate the JWT against the user details
            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                // Create an authentication object and set it in the context
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        // Pass the request and response to the next filter in the chain
        chain.doFilter(request, response);
    }
}
