package br.com.casa_moreno.casa_moreno_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;

    public SecurityConfig(CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler) {
        this.customOAuth2AuthenticationSuccessHandler = customOAuth2AuthenticationSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // COLOCAR O permitAll DEPOIS DOS ENDPOINTS QUE PRECISAM DE AUTENTICAÇÃO SENÃO DÁ UMA PICA DO CARALHO
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/products/create").hasRole("ADMIN")
                        .requestMatchers("/products/update").hasRole("ADMIN")
                        .requestMatchers("/products/delete/**").hasRole("ADMIN")
                        .requestMatchers("/products/list-all").hasRole("ADMIN")
                        .requestMatchers("/products/{id}/promotional").hasRole("ADMIN")
                        .requestMatchers("/products/{id}/images/set-main").hasRole("ADMIN")
                        .requestMatchers("/products/{id}/images/delete").hasRole("ADMIN")
                        .requestMatchers("/products/find-by-category", "/products/categories", "/products/{id}", "/promotional").permitAll()

                        //USERS
                        .requestMatchers("/users/{userId}/profile-picture").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/users/username/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/users/find-all-users").hasAnyRole("ADMIN")
                        .requestMatchers("/users/update").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/users/delete/**").hasRole("ADMIN")
                        .requestMatchers("/users/create", "/users/forgot-password/**", "/users/reset-password").permitAll()

                        //AI
                        .requestMatchers(HttpMethod.POST, "/ai/chat").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/ai/organize-description").hasRole("ADMIN")

                        //GLOBAL
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**", "/casa-moreno-docs/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()

                        .anyRequest().authenticated())


                .oauth2Login(oauth2 -> {
                    oauth2.successHandler(customOAuth2AuthenticationSuccessHandler);
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}