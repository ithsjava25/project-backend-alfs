package org.example.alfs.config;

import org.example.alfs.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {this.jwtFilter = jwtFilter;}

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Only for development (H2 console support)
                .securityMatcher("/**")

                .csrf(csrf -> csrf.disable())

                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // stateless jwt
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


                // Authorization strategy:
                // - JWT is used for authentication (identifying the user)
                // - User roles are NOT trusted from the JWT
                // - Roles are always loaded from the database
                // This ensures that permission changes take effect immediately
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/signup").permitAll()
                        .requestMatchers("/auth/logout").permitAll()
                        .requestMatchers("/auth/hash").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/startPage", "/").permitAll()
                        .requestMatchers("/tickets/create").permitAll()
                        .requestMatchers("/tickets/previewTicket").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/files/upload").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        //allow access to endpoints during development
                        .requestMatchers("/tickets/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/login", "/login-form").permitAll()
                        .requestMatchers("/signup", "/signup-form").permitAll()
                        .anyRequest().authenticated()
                )

                // disable DEFAULT LOGIN
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())


                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}