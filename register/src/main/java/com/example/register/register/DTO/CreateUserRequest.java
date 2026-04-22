package com.example.register.register.DTO;

public record CreateUserRequest(
        String firstName,
        String lastName,
        String username,
        String email,
        Long roleId,
        Long teamId,
        Long sectionId,
        Long positionId
) {
}
