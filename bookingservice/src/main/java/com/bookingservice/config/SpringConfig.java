package com.bookingservice.config;

import com.bookingservice.service.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SpringConfig {
    @Value("${fe-server.url}")
    private String serverFe;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOidcUserService customOidcUserService) throws Exception {
        http
            // 1. Disable CSRF for a stateless JSON API
            .csrf(csrf -> csrf.disable())

            // 2. Enable CORS if needed
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))


            // Cấu hình phân quyền
            .authorizeHttpRequests(auth -> auth
                // Cho phép truy cập không cần auth tất cả HTTP methods đến /product/v1/checkout
                .requestMatchers("/product/v1/checkout", "/product/v1/checkout/**").permitAll()
                // Các đường dẫn public khác
                .requestMatchers("/", "/login").permitAll()
                // swagger & openapi
                .requestMatchers(
                    "/api-docs/**",
                    "/api-docs",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"


                ).permitAll()
                // CRUD endpoints
                .requestMatchers(
                    "/events",
                    "/events/**",
                    "/bookings",
                    "/bookings/*"



                ).permitAll()

                    // Bất kỳ request nào khác phải xác thực
                .anyRequest().authenticated()
            )

//             Cấu hình OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(u -> u.oidcUserService(customOidcUserService))
                .loginPage(serverFe + "login")
                .successHandler((request, response, authentication) ->
                    response.sendRedirect(serverFe)
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                // mặc định là POST /logout
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .deleteCookies("JSESSIONID")           // xoá cookie phiên
                .invalidateHttpSession(true)           // huỷ session server
                .logoutSuccessHandler((req,res,auth) -> res.setStatus(200)) // SPA tự xử lý
            )
        ;

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:5173","http://172.187.193.117:5173"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all endpoints (or narrow to /product/v1/checkout if you like)
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
