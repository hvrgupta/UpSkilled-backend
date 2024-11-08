package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AuthRequest;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.UserService;
import com.software.upskilled.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            Users user = new Users();
            user.setEmail(userDTO.getEmail());
            user.setPassword(userDTO.getPassword());
            user.setRole(userDTO.getRole().toUpperCase());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setDesignation(userDTO.getDesignation());
            if("INSTRUCTOR".equalsIgnoreCase(userDTO.getRole())) {
                user.setStatus(Users.Status.INACTIVE);
                usersDetailsService.createUser(user);
            }
            else if("EMPLOYEE".equalsIgnoreCase(userDTO.getRole())){
                user.setStatus(Users.Status.ACTIVE);
                usersDetailsService.createUser(user);
            }else {
                throw new Exception("Role specified is incorrect");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration failed : " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
            );
            if(authentication.isAuthenticated()) {
                Users user = usersDetailsService.findUserByEmail(authRequest.getEmail());
                return jwtUtil.generateToken(user.getEmail(),user.getFirstName(),user.getLastName(),user.getRole(),user.getStatus());
            }

        } catch (Exception e) {
            throw new Exception("Invalid username or password", e);
        }
        return "";
    }

    @PostMapping("/update-profile")
    public ResponseEntity<String> updateUser(@RequestBody CreateUserDTO userDTO, Authentication authentication) {
        try {
            Users user = usersDetailsService.findUserByEmail(authentication.getName());

            if(user == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User Not Found!");
            }

            user.setDesignation(userDTO.getDesignation());
            usersDetailsService.updateUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Updation failed : " + e.getMessage());
        }
    }
}
