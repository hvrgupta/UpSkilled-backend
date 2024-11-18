package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AuthRequest;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.UserService;
import com.software.upskilled.utils.JWTUtil;
import com.software.upskilled.utils.TokenBlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService usersDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenBlackListService blacklistService;

    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/user")
    public CreateUserDTO getCurrentUser(@AuthenticationPrincipal Users user) {
        if(user == null) throw new UsernameNotFoundException("user not logged in");
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setPassword("*******");
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setDesignation(user.getDesignation());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody CreateUserDTO userDTO) {
        try {

//          Check if email matches the required format
            if (!userDTO.getEmail().matches("^[A-Za-z0-9._%+-]+@upskilled\\.com$")) {
                return ResponseEntity.badRequest().body("Invalid email format. Email must be in the format username@upskilled.com");
            }

            Users user = new Users();

            if(userDTO.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Password should be greater than 6");
            }

            if(userDTO.getEmail().isBlank() || userDTO.getFirstName().isBlank() || userDTO.getLastName().isBlank()) {
                return ResponseEntity.badRequest().body("Email, First Name or Last Name missing!");
            }

            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setDesignation(userDTO.getDesignation());

            if("INSTRUCTOR".equalsIgnoreCase(userDTO.getRole())) {
                user.setRole(userDTO.getRole().toUpperCase());
                user.setStatus(Users.Status.INACTIVE);
                usersDetailsService.createUser(user);
            }
            else if("EMPLOYEE".equalsIgnoreCase(userDTO.getRole())){
                user.setRole(userDTO.getRole().toUpperCase());
                user.setStatus(Users.Status.ACTIVE);
                usersDetailsService.createUser(user);
            }else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role specified is incorrect");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed : " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );

            if(authentication.isAuthenticated()) {
                Users user = usersDetailsService.findUserByEmail(authRequest.getEmail());
                String token = jwtUtil.generateToken(user);
                return ResponseEntity.ok(token);
            }else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid username or password");
            }

        } catch (BadCredentialsException e) {
            // Handle invalid username or password exception
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        } catch (Exception e) {
            // Catch any other unexpected exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        blacklistService.blacklistToken(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateUser(@RequestBody CreateUserDTO userDTO, Authentication authentication) {
        try {
            Users user = usersDetailsService.findUserByEmail(authentication.getName());

            if(userDTO.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Password should be greater than 6");
            }

            user.setPassword(userDTO.getPassword());
            user.setDesignation(userDTO.getDesignation());

            usersDetailsService.updateUser(user);

            return ResponseEntity.status(HttpStatus.CREATED).body("User updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Updation failed : " + e.getMessage());
        }
    }
}
