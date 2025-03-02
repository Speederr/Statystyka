package com.example.register.register.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Data
@Table(name = "user_message")
public class UserMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;      // Nadawca wiadomości
    private String recipient;   // Odbiorca wiadomości
    private String subject;     // Temat wiadomości

    @Column(columnDefinition = "TEXT")
    private String content;     // Treść wiadomości

    @Column(name = "read_by_sender", nullable = false)
    private boolean readBySender = false; // Czy nadawca oznaczył wiadomość jako przeczytaną

    @Column(name = "read_by_recipient", nullable = false)
    private boolean readByRecipient = false; // Czy odbiorca oznaczył wiadomość jako przeczytaną

    @Column(name = "deleted_by_sender", nullable = false)
    private boolean deletedBySender = false;

    @Column(name = "deleted_by_recipient", nullable = false)
    private boolean deletedByRecipient = false;

    private LocalDateTime timestamp; // Data i godzina wysłania wiadomości
}
