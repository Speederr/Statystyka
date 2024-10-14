package com.example.register.register.config;

import com.example.register.register.repository.FormUsersRepository;
import com.example.register.register.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Sprawdź, czy użytkownik loguje się po raz pierwszy
        boolean isFirstLogin = checkIfFirstLogin(authentication);

        // Jeśli to pierwsze logowanie, przekieruj na stronę zmiany hasła
        if (isFirstLogin) {
            getRedirectStrategy().sendRedirect(request, response, "/firstLogin");
        } else {
            // W przeciwnym razie, przekieruj na domyślną stronę
            getRedirectStrategy().sendRedirect(request, response, "/index");
        }
    }

    private boolean checkIfFirstLogin(Authentication authentication) {

        String username = authentication.getName();
        return userService.isFirstLogin(username);
    }
}
