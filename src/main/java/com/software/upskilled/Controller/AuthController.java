package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody CreateUserDTO userDTO) {
        try {
            Users user = new Users();
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());
            user.setRole(userDTO.getRole().toUpperCase());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setDesignation(userDTO.getDesignation());
            if("INSTRUCTOR".equalsIgnoreCase(userDTO.getRole())) {
                user.setStatus(Users.Status.INACTIVE);
                userService.createUser(user);
            }
            else if("EMPLOYEE".equalsIgnoreCase(userDTO.getRole())){
                user.setStatus(Users.Status.ACTIVE);
                userService.createUser(user);
            }else {
                throw new Exception("Role specified is incorrect");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed : " + e.getMessage());
        }
    }
}
