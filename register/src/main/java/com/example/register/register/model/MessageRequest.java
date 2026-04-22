package com.example.register.register.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Data
public class MessageRequest {
    private String recipient;
    private String subject;
    private String message;
}
