package com.example.register.register.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;

    @NotBlank(message = "Imię nie może być puste")
    private String firstName;

    @NotBlank(message = "Nazwisko nie może być puste")
    private String lastName;

    private String username;

    private String email;

    private byte[] avatarUrl;

    @NotNull(message = "Identyfikator roli jest wymagany")
    private Long roleId;

    public UserDto(String firstName, String lastName, String username, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
    }

    public UserDto(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
