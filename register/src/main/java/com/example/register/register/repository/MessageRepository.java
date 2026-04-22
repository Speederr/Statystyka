package com.example.register.register.repository;

import com.example.register.register.model.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<UserMessage, Long> {

    List<UserMessage> findByRecipientAndDeletedByRecipientFalse(String recipient);
    List<UserMessage> findBySenderAndDeletedBySenderFalse(String sender);

    int countByRecipientAndReadByRecipientFalse(String recipient);


}
