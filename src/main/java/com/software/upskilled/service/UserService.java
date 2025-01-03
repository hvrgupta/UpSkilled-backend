package com.software.upskilled.service;


import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for handling user-related operations, including user creation, updating, and retrieval.
 * Implements the `UserDetailsService` interface for Spring Security to load user details by username (email).
 * Provides methods for managing users by their roles and statuses, such as finding instructors, active/inactive instructors, and performing CRUD operations.
 * Passwords are securely encoded before being saved or updated.
 */
@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = userRepository.findByEmail(username);
        if(userDetails == null) {
            throw new UsernameNotFoundException("User does not exists");
        }
        return userDetails;
    }

    public void createUser(Users appUser) throws Exception {
        Users user = findUserByEmail(appUser.getEmail());
        if(user != null) {
            throw new Exception("Email already exists");
        }
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        userRepository.save(appUser);
    }

    public void updateUser(Users appUser) throws Exception {
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        userRepository.save(appUser);
    }

    public Users findUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Users findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<Users> getInstructors() {
        return userRepository.findByRole("INSTRUCTOR");
    }

    public List<Users> getActiveInstructors() {
        return userRepository.findByRoleAndStatus("INSTRUCTOR", Users.Status.ACTIVE);
    }

    public List<Users> getInactiveInstructors() {
        return userRepository.findByRoleAndStatus("INSTRUCTOR", Users.Status.INACTIVE);
    }

    public Users saveUser(Users user) {
        return userRepository.save(user);
    }

    public void deleteUser(Users user){ userRepository.delete(user); }
}
