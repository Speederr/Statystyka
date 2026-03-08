package com.example.register.register.config;

import com.example.register.register.model.User;
import com.example.register.register.service.AttendanceService;
import com.example.register.register.service.UserService;
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

    @Autowired
    private AttendanceService attendanceService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user.isFirstLogin()) {
            getRedirectStrategy().sendRedirect(request, response, "/firstLogin");
            return;
        }

        String clientIp = request.getRemoteAddr();
        attendanceService.recordAttendanceAfterLogin(username, clientIp);

        boolean isManager = authentication.getAuthorities().stream()
                .anyMatch(ga -> ga.getAuthority().equals("ROLE_MANAGER"));

        if (isManager) {
            getRedirectStrategy().sendRedirect(request, response, "/efficiency");
        } else {
            getRedirectStrategy().sendRedirect(request, response, "/index");
        }
    }
}