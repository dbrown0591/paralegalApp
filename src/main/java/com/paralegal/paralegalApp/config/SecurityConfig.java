package com.paralegal.paralegalApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
                        "/", "/error", "/health", "/actuator/**"
                        ).permitAll()
                        .requestMatchers(
                                "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**"
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
                        .anyRequest().authenticated()
                )
                // simple basic auth for now
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))//Remove this when database is up and running
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
    // In-memory users for testing. Remove if you keep spring.security.user.* in progress
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService users(){
       UserDetails admin = User.withUsername("admin")
               .password(passwordEncoder().encode("admin"))
               .roles("ADMIN")
               .build();
       return new InMemoryUserDetailsManager(admin);

    }
}
