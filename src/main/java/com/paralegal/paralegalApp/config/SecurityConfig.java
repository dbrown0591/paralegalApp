package com.paralegal.paralegalApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth-> auth
                        // ---PUBLIC endpoints (read-only) ---
                        .requestMatchers(
                        "/", "/error", "health", "/actuator/**"
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**", "swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        // GETs for your resources are public
                        .requestMatchers(HttpMethod.GET, "/api/incidents/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll()
                        // --- PROTECTED endpoints (anything that changes data) ----
                        .requestMatchers(HttpMethod.POST, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.PUT,"/api/**" ).authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/**").authenticated()

                        // user endpoints: all require auth
                        .requestMatchers("/api/users/**").authenticated()

                        //everything else: block unless permitted above
                        .anyRequest().denyAll()
                )
                // simple basic auth for now
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    // In-memory users for testing. Remove if you keep spring.security.user.* in progress

    public UserDetailsService users(){
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password("{noop}admin123").roles("ADMIN").build(),
                User.withUsername("user").password("{noop}admin123").roles("USER").build()
        );
    }
}
