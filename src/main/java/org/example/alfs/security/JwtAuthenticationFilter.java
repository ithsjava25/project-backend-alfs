package org.example.alfs.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.alfs.entities.User;
import org.example.alfs.repositories.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // Skips filter for LOGIN & H2
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth") || path.startsWith("/h2-console");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        String jwt = null;
        // Try to read JWT from Authorization header (used by API/Postman)
        // Ignore empty Bearer tokens so cookie fallback still works
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String bearer = authHeader.substring(7).trim();
            if (!bearer.isEmpty()) {
                jwt = bearer;
            }
        }

        // If no valid token in header, try reading JWT from cookies (browser)
        if ((jwt == null || jwt.isBlank()) && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("JWT".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        // if still no token, continue without auth
        if(jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }


        String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // IMPORTANT:
            // We do NOT trust the role stored in the JWT.
            // Instead, we always load the user's role from the database.
            //
            // This ensures that if a user's role changes (e.g. ADMIN → REPORTER),
            // the change takes effect immediately, even if the old JWT is still valid.
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    new org.springframework.security.core.userdetails.User(
                            user.getUsername(),
                            user.getPasswordHash(),
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );

            boolean valid = jwtService.isTokenValid(jwt, user);

            if (valid) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new org.springframework.security.web.authentication.WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}