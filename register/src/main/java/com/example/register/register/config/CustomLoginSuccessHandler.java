package com.example.register.register.config;

import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.AttendanceService;
import com.example.register.register.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.LocalDate;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private AttendanceService attendanceService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        String username = authentication.getName();

        if (!attendanceService.hasAttendanceForToday(username)) {
            String clientIp = request.getRemoteAddr();
            attendanceService.recordAttendanceWithIp(username, clientIp);
        }

        boolean isFirstLogin = userService.isFirstLogin(username);

        if (isFirstLogin) {
            // 🔁 Nie zmieniamy flagi tutaj – użytkownik musi najpierw zmienić hasło
            getRedirectStrategy().sendRedirect(request, response, "/firstLogin");
        } else {
            getRedirectStrategy().sendRedirect(request, response, "/index");
        }
    }




    private boolean checkIfFirstLogin(Authentication authentication) {

        String username = authentication.getName();
        return userService.isFirstLogin(username);
    }
}
