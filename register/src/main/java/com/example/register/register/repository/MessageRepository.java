package com.example.register.register.repository;

import com.example.register.register.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<UserMessage, Long> {
    List<UserMessage> findByRecipient(String recipient); // Pobieranie wiadomości dla użytkownika
    List<UserMessage> findBySender(String sender);       // Pobieranie wiadomości wysłanych
    List<UserMessage> findByRecipientAndDeletedByRecipientFalse(String recipient);
    List<UserMessage> findBySenderAndDeletedBySenderFalse(String sender);

    int countByRecipientAndReadByRecipientFalse(String recipient);


}
