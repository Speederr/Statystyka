package com.example.register.register.config;

import com.example.register.register.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieClearingLogoutHandler cookies = new CookieClearingLogoutHandler("JSESSIONID");

        http
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Publiczne endpointy
                        .requestMatchers("/register", "/login", "/firstLogin", "/restorePassword",
                                "/api/user/restorePassword", "/css/**", "/js/**", "/images/**").permitAll()

                        // 🔒 REST API dla wiadomości - dostępne dla zalogowanych użytkowników
                        .requestMatchers(HttpMethod.GET, "/api/messages/received").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/messages/sent").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/messages/**").authenticated()

                        .requestMatchers(HttpMethod.POST, "/api/messages/send").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/message/{messageId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/message/bulk-delete").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/messages/mark-as-read").authenticated() // Wymaga zalogowania
                        .requestMatchers(HttpMethod.PUT, "/api/messages/mark-as-read/{messageId}").authenticated()

                        // 🔒 Endpointy wymagające autoryzacji
                        .requestMatchers(HttpMethod.POST, "/api/processes/favorites/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/efficiency/calculate/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/saved-data/save").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/processes/saveNewProcess", "/api/user/addUser",
                                "/api/user/deleteUsers", "/api/teams", "/api/teams/saveNewTeam", "/api/sections/saveNewSection").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/user/avatar").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/user/changePasswordInSettings").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/user/avatar").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/processes/update/**").hasRole("ADMIN")
                        .requestMatchers("/adminPanel").hasRole("ADMIN")
                        .requestMatchers("/averageTime").hasRole("ADMIN")
                        .requestMatchers("/efficiency").hasAnyRole("ADMIN", "MANAGER", "COORDINATOR")
                        .requestMatchers("/index", "/changePassword", "/settings", "/api/notifications/count").authenticated()
                        .anyRequest().authenticated()
                )

                // 🟢 Konfiguracja logowania
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/login?error=invalidCredentials")
                        .permitAll()
                )

                // 🔴 Konfiguracja wylogowania
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?success=logout")
                        .addLogoutHandler(cookies)
                )

                // 🛡️ Wyłączenie CSRF dla API
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/api/items/save",
                                "/api/user/restorePassword",
                                "/api/processes/favorites/**",
                                "/api/processes/update/**",
                                "/api/saved-data/save",
                                "/api/efficiency/calculate/**",
                                "/api/user/avatar",
                                "/api/user/changePasswordInSettings",
                                "/api/user/addUser",
                                "/api/messages/send",
                                "/api/messages/received",
                                "/api/messages/sent",
                                "/api/messages/{messageId}",
                                "/api/message/bulk-delete",
                                "/api/user/deleteUsers",
                                "/api/messages/mark-as-read",
                                "/api/messages/mark-as-read/{messageId}",
                                "/api/messages/**",
                                "/api/teams",
                                "/api/teams/saveNewTeam",
                                "/api/sections/saveNewSection"
                        )
                );

        return http.build();
    }

    // Konfiguracja uwierzytelniania
    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder);
    }
}
