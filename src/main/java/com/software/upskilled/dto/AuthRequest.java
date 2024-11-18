package com.software.upskilled.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    /**
     * Data Transfer Object (DTO) for holding the authentication request data.
     * This DTO is used to transfer the user's login credentials (email and password).
     */
    private String email;
    private String password;
}
