package com.example.register.register.service;

import com.example.register.register.model.UserMessage;
import com.example.register.register.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserMessageService {

    private final MessageRepository messageRepository;

    public UserMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<UserMessage> getReceivedMessages(String recipient) {
        return messageRepository.findByRecipientAndDeletedByRecipientFalse(recipient);
    }

    public List<UserMessage> getSentMessages(String sender) {
        return messageRepository.findBySenderAndDeletedBySenderFalse(sender);
    }

}

