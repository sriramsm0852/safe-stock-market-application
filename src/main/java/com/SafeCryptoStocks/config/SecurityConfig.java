package com.SafeCryptoStocks.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.SafeCryptoStocks.model.User;
import com.SafeCryptoStocks.model.UserPrincipal;
import com.SafeCryptoStocks.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
            .cors(Customizer.withDefaults()) // Allow CORS requests
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/signup", "/registerUser", "/loginUser", "/forgotPwd", "/forgotPassword",
                    "/create-portfolio", "/learn","/dash/trending-cryptocurrency","/dash/crypto-news","/dash/cryptocurrency", "/cryptocurrency", "/api/cryptocurrencies", "/budget",
                    "/market", "/portfolio", "/verifyPasswordResetOtp", "/verifyOtp", "/resetPassword",
                    "/verify-otp", "/portfolios", "/portfolios/**", "/dashboard", "/dashboard/**",
                    "/stock", "/stock/**", "/css/**", "/js/**", "/image/**", "/webjars/**","/dummy-stock","/create-portfolio/{userId}",
                    "/selectQuantity","/dummy-stock/update","/stock/bulk-insert/**",
                    "/budgets","/budgets/**","/budgets/{id}/expenses","/api/**","/upload-profile-pic","/profile"
                		).permitAll() // Allow public endpoints
                .anyRequest().authenticated() // Require authentication for all other endpoints
            )
            .logout(logout -> logout
                .logoutUrl("/logout") // URL for logout
                .logoutSuccessUrl("/login?logout") // Redirect after logout
                .invalidateHttpSession(true) // Invalidate session
                .deleteCookies("JSESSIONID") // Clear session cookie
                .permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session management
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT filter
            .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        provider.setUserDetailsService(userDetailsService());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByEmail(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return new UserPrincipal(user.getId(), user.getUsername(), user.getPassword());
        };
    }
}
