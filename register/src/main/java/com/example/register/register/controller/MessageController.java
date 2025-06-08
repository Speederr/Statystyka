package com.example.register.register.controller;
import com.example.register.register.model.MessageRequest;
import com.example.register.register.model.User;
import com.example.register.register.model.UserMessage;
import com.example.register.register.repository.MessageRepository;
import com.example.register.register.repository.UserRepository;
import com.example.register.register.service.EmailService;
import com.example.register.register.service.UserMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserMessageService userMessageService;

    public MessageController(MessageRepository messageRepository, UserRepository userRepository, EmailService emailService, SimpMessagingTemplate messagingTemplate, UserMessageService userMessageService) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.messagingTemplate = messagingTemplate;
        this.userMessageService = userMessageService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody MessageRequest messageRequest, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Użytkownik niezalogowany"));
        }

        String senderUsername = principal.getName();

        // 🔹 Pobranie e-maila nadawcy na podstawie username
        String senderEmail = userRepository.findByUsername(senderUsername)
                .map(User::getEmail)
                .orElse(null);

        if (senderEmail == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nie znaleziono e-maila dla użytkownika."));
        }

        System.out.println("🔹 Nadawca wiadomości: " + senderUsername + " | E-mail: " + senderEmail);

        // Pobranie listy odbiorców (podzielonej po przecinku)
        List<String> recipients = Arrays.stream(messageRequest.getRecipient().split(","))
                .map(String::trim)
                .toList();

        // Sprawdzenie, czy wszyscy odbiorcy istnieją
        List<String> invalidRecipients = recipients.stream()
                .filter(email -> userRepository.findByEmail(email).isEmpty())
                .collect(Collectors.toList());

        if (!invalidRecipients.isEmpty()) {
            System.out.println("❌ Następujący odbiorcy nie istnieją: " + String.join(", ", invalidRecipients));
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nie znaleziono odbiorców: " + String.join(", ", invalidRecipients)));
        }

        // Zapisywanie wiadomości do bazy dla każdego odbiorcy
        recipients.forEach(recipient -> {
            UserMessage message = new UserMessage();
            message.setSender(senderEmail); // 🔹 Ustawiamy e-mail nadawcy zamiast username
            message.setRecipient(recipient);
            message.setSubject(messageRequest.getSubject());
            message.setContent(messageRequest.getMessage());
            message.setTimestamp(LocalDateTime.now());

            messageRepository.save(message);
        });

        System.out.println("✅ Wiadomość została zapisana dla wszystkich odbiorców!");

        // Wysyłanie e-maila do każdego odbiorcy
        try {
            for (String recipient : recipients) {
                emailService.sendEmail(
                        recipient,
                        messageRequest.getSubject(),
                        messageRequest.getMessage()
                );
                System.out.println("📧 E-mail został wysłany do: " + recipient);
                messagingTemplate.convertAndSend("/topic/notifications", "new_message");
            }
        } catch (Exception e) {
            System.out.println("❌ Błąd wysyłania e-maila: " + e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Nie udało się wysłać e-maila do wszystkich odbiorców."));
        }

        return ResponseEntity.ok(Collections.singletonMap("success", "Wiadomość została wysłana do wszystkich odbiorców."));
    }


    @GetMapping("/received")
    public ResponseEntity<?> getReceivedMessages(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Collections.singletonMap("error", "Nie jesteś zalogowany!"));
        }

        String username = principal.getName();
        System.out.println("🔍 Username zalogowanego użytkownika: " + username);

        // Pobierz email użytkownika na podstawie username
        String email = userRepository.findByUsername(username)
                .map(User::getEmail) // Pobranie e-maila z encji User
                .orElse(null);

        if (email == null) {
            return ResponseEntity.status(404).body(Collections.singletonMap("error", "Nie znaleziono e-maila dla użytkownika."));
        }

        System.out.println("📩 E-mail użytkownika: " + email);

        // Pobranie wiadomości na podstawie e-maila
//        List<UserMessage> receivedMessages = messageRepository.findByRecipient(email);
        List<UserMessage> receivedMessages = messageRepository.findByRecipientAndDeletedByRecipientFalse(email);
        System.out.println("📥 Odebrane wiadomości: " + receivedMessages.size());

        // 🔹 Modyfikujemy dane przed zwróceniem
        List<Map<String, Object>> response = receivedMessages.stream()
                .map(message -> {
                    Map<String, Object> mappedMessage = new HashMap<>();
                    mappedMessage.put("id", message.getId());
                    mappedMessage.put("sender", message.getSender());
                    mappedMessage.put("recipient", message.getRecipient());
                    mappedMessage.put("subject", message.getSubject());
                    mappedMessage.put("content", message.getContent());
                    mappedMessage.put("timestamp", message.getTimestamp());
                    mappedMessage.put("read", message.isReadByRecipient()); // 🔹 Frontend oczekuje `read`, więc przekazujemy `readByRecipient`
                    return mappedMessage;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sent")
    public ResponseEntity<?> getSentMessages(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Użytkownik niezalogowany"));
        }

        String currentUsername = principal.getName();

        // 🔹 Pobranie e-maila użytkownika na podstawie username
        String currentEmail = userRepository.findByUsername(currentUsername)
                .map(User::getEmail)
                .orElse(null);

        if (currentEmail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Nie znaleziono e-maila dla użytkownika."));
        }

        System.out.println("📤 Pobieranie wysłanych wiadomości dla: " + currentEmail);

        // Pobranie wysłanych wiadomości na podstawie e-maila nadawcy
        List<UserMessage> sentMessages = messageRepository.findBySenderAndDeletedBySenderFalse(currentEmail);

        // 🔹 Mapowanie danych przed zwróceniem do frontendu
        List<Map<String, Object>> response = sentMessages.stream()
                .map(message -> {
                    Map<String, Object> mappedMessage = new HashMap<>();
                    mappedMessage.put("id", message.getId());
                    mappedMessage.put("sender", message.getSender());
                    mappedMessage.put("recipient", message.getRecipient());
                    mappedMessage.put("subject", message.getSubject());
                    mappedMessage.put("content", message.getContent());
                    mappedMessage.put("timestamp", message.getTimestamp());
                    mappedMessage.put("read", message.isReadBySender()); // 🔹 Oznaczamy wiadomości wysłane jako przeczytane po stronie nadawcy
                    return mappedMessage;
                })
                .toList();

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId, Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Użytkownik niezalogowany."));
        }

        String currentEmail = userRepository.findByUsername(principal.getName())
                .map(User::getEmail)
                .orElse(null);

        if(currentEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nie znaleziono e-maila użytkownika."));
        }

        Optional<UserMessage> optionalMessage = messageRepository.findById(messageId);

        if (optionalMessage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Wiadomość nie istnieje."));
        }

        UserMessage message = optionalMessage.get();

        if (message.getSender().trim().equalsIgnoreCase(currentEmail.trim())) {
            message.setDeletedBySender(true);
        }

        if(message.getRecipient().trim().equalsIgnoreCase(currentEmail.trim())) {
            message.setDeletedByRecipient(true);
        }

        messageRepository.save(message);
        return ResponseEntity.ok(Map.of("success", "Wiadomość została usunięta."));

    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity<?> deleteMultipleMessages(@RequestBody List<Long> messageIds, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Użytkownik niezalogowany"));
        }

        if (messageIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nie podano żadnych ID do usunięcia."));
        }

        // Pobranie e-maila użytkownika
        String currentEmail = userRepository.findByUsername(principal.getName())
                .map(User::getEmail)
                .orElse(null);

        if (currentEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nie znaleziono e-maila użytkownika."));
        }

        List<UserMessage> messagesToUpdate = messageRepository.findAllById(messageIds);

        if (messagesToUpdate.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Nie znaleziono wiadomości do usunięcia."));
        }

        for (UserMessage message : messagesToUpdate) {

            // Jeśli użytkownik jest nadawcą, oznacz wiadomość jako usuniętą przez nadawcę
            if (message.getSender().trim().equalsIgnoreCase(currentEmail.trim())) {
                message.setDeletedBySender(true);
            }

            // Jeśli użytkownik jest odbiorcą, oznacz wiadomość jako usuniętą przez odbiorcę
            if (message.getRecipient().trim().equalsIgnoreCase(currentEmail.trim())) {
                message.setDeletedByRecipient(true);
            }

        }
            messageRepository.saveAll(messagesToUpdate);

        return ResponseEntity.ok(Map.of("success", "Zaznaczone wiadomości zostały usunięte dla Ciebie"));
    }


    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Integer>> getUnreadMessagesCount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("count", 0));
        }

        String username = principal.getName(); // 🛠 Pobieramy LOGIN użytkownika
        System.out.println("🔹 Sprawdzanie użytkownika: " + username);

        // 🛠 Pobierz email użytkownika na podstawie loginu
        String userEmail = userRepository.findByUsername(username)
                .map(User::getEmail) // Pobranie e-maila z encji User
                .orElse(null);

        if (userEmail == null) {
            System.out.println("⚠️ Błąd: Nie znaleziono emaila dla użytkownika " + username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("count", 0));
        }

        System.out.println("📩 Sprawdzanie nieprzeczytanych wiadomości dla: " + userEmail);

        // 🛠 Używamy emaila zamiast loginu
        int unreadCount = messageRepository.countByRecipientAndReadByRecipientFalse(userEmail);
        System.out.println("🔢 Liczba nieprzeczytanych wiadomości: " + unreadCount);

        return ResponseEntity.ok(Map.of("count", unreadCount));
    }

    @PutMapping("/mark-as-read")
    public ResponseEntity<?> markMessagesAsRead(
            @RequestBody Map<String, List<Long>> request,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Użytkownik niezalogowany"));
        }

        String currentUsername = principal.getName();

        // 🔹 Pobranie e-maila użytkownika na podstawie loginu
        String currentEmail = userRepository.findByUsername(currentUsername)
                .map(User::getEmail)
                .orElse(null);

        if (currentEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nie znaleziono e-maila dla użytkownika."));
        }

        List<Long> messageIds = request.get("messageIds");

        if (messageIds == null || messageIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Brak ID wiadomości do oznaczenia."));
        }

        List<UserMessage> messages = messageRepository.findAllById(messageIds);

        if (messages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Żadne wiadomości nie zostały znalezione."));
        }

        // 🔹 Oznacz wiadomości jako przeczytane przez odbiorcę lub nadawcę
        messages.forEach(message -> {
            if (message.getRecipient().trim().equalsIgnoreCase(currentEmail.trim())) {
                message.setReadByRecipient(true);
            }
            if (message.getSender().trim().equalsIgnoreCase(currentEmail.trim())) {
                message.setReadBySender(true);
            }
        });

        messageRepository.saveAll(messages);

        List<UserMessage> updatedMessages = messageRepository.findAllById(messageIds);

        updatedMessages.forEach(msg ->
                System.out.println("ID: " + msg.getId() + " | Read by Recipient: " + msg.isReadByRecipient() + " | Read by Sender: " + msg.isReadBySender())
        );

        return ResponseEntity.ok(Map.of("success", "Wiadomości oznaczone jako przeczytane"));
    }

    @PutMapping("/mark-as-read/{messageId}")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId, Principal principal) {
        if(principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Użytkownik niezalogowany"));
        }

        String currentEmail = userRepository.findByUsername(principal.getName())
                .map(User::getEmail)
                .orElse(null);

        if(currentEmail == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Nie znaleziono e-maila użytkownika."));
        }
        Optional<UserMessage> optionalMessage = messageRepository.findById(messageId);

        if (optionalMessage.isEmpty())  {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Wiadomość nie istnieje."));
        }

        UserMessage message = optionalMessage.get();

        if(message.getRecipient().trim().equalsIgnoreCase(currentEmail.trim())) {
            message.setReadByRecipient(true);
        }
        if(message.getSender().trim().equalsIgnoreCase(currentEmail.trim())) {
            message.setReadBySender(true);
        }

        messageRepository.save(message);

        return ResponseEntity.ok(Map.of("success", "Wiadomość oznaczona jako przeczytana"));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<UserMessage> getMessageById(@PathVariable Long messageId, Principal principal) {
        System.out.println("🔹 Otrzymano żądanie GET dla wiadomości ID: " + messageId); // LOG

        String username = principal.getName(); // Pobranie loginu użytkownika
        System.out.println("🔹 Żądanie od użytkownika (login): " + username); // LOG

        // Pobranie użytkownika i jego e-maila
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            System.out.println("❌ Użytkownik nie znaleziony.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = optionalUser.get().getEmail(); // Pobranie e-maila użytkownika
        System.out.println("🔹 E-mail zalogowanego użytkownika: " + userEmail);

        // Pobranie wiadomości z bazy danych
        Optional<UserMessage> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isEmpty()) {
            System.out.println("❌ Wiadomość nie istnieje.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        UserMessage message = optionalMessage.get();

        // Sprawdzenie, czy użytkownik jest odbiorcą lub nadawcą wiadomości
        if (!message.getRecipient().equals(userEmail) && !message.getSender().equals(userEmail)) {
            System.out.println("❌ Użytkownik nie ma uprawnień do przeglądania tej wiadomości.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        return ResponseEntity.ok(message);
    }




}
