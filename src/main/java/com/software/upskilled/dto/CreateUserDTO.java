package com.software.upskilled.dto;

import com.software.upskilled.Entity.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
    /**
     * Data Transfer Object (DTO) for creating a new user.
     * This DTO is used to transfer user data during the user registration process.
     */
    private Long id;
    private String email;
    private String password;
    private String role;
    private String firstName;
    private String lastName;
    private String designation;
    private Users.Status status;
}
