package com.example.commerce.config;


import com.example.commerce.services.CustomOAuth2UserService;
import com.example.commerce.services.CustomUserService;
import com.example.commerce.services.JwtService;
import com.example.commerce.services.TokenBlacklistService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailure oAuth2AuthenticationFailure;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, 
                         OAuth2AuthenticationFailure oAuth2AuthenticationFailure, 
                         CustomOAuth2UserService customOAuth2UserService) {
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailure = oAuth2AuthenticationFailure;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                           TokenBlacklistService tokenBlacklistService,
                                           CustomAuthenticationEntryPoint authenticationEntryPoint,
                                           CustomAccessDeniedHandler accessDeniedHandler,
                                           CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/api/*/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/products/public/**",
                                        "/api/users/public/**",
                                        "/api/categories/public/**",
                                        "/api/oauth2/**",
                                        "/login",
                                        "/oauth2/**",
                                        "/error",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/graphiql/**"
                                )
                                .permitAll()
                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth2 -> oauth2.userInfoEndpoint(
                        userInfo -> userInfo.userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailure)
                )
                .formLogin(Customizer.withDefaults())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(CustomUserService customUserService, PasswordEncoder passwordEncoder){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(customUserService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        try {
            return config.getAuthenticationManager();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get AuthenticationManager", e);
        }
    }

}