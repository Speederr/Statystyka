package com.example.register.register.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendUserCreationMail(String to, String name, String last_name, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Utworzono nowego pracownika.");
        message.setText("Witaj " + name + " " + last_name + ",\n\n"
                + "Twoje konto do statystyki zostało utworzone.\n"
                + "Login: " + username + "\n"
                + "Hasło tymczasowe: " + password + "\n\n"
                + "Proszę zmienić hasło po zalogowaniu.");

        mailSender.send(message);
    }

    public void sendTemporaryPasswordEmail(String email, String temporaryPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("🔒 Reset hasła - Twoje nowe hasło tymczasowe");
        message.setText("Twoje nowe tymczasowe hasło to: " + temporaryPassword +
                "\n\nZaloguj się i zmień je na własne.");

        mailSender.send(message);
    }

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("twojemail@gmail.com"); // Możesz podać adres nadawcy

        mailSender.send(message);
    }
}
