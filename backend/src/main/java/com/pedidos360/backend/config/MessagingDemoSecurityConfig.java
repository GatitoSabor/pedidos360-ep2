package com.pedidos360.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad exclusiva para la demostración local de mensajería.
 * El perfil normal conserva SecurityConfig y la validación JWT de Cognito.
 */
@Configuration
@Profile("messaging-demo")
public class MessagingDemoSecurityConfig {

    @Bean
    SecurityFilterChain messagingDemoSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
