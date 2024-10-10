package com.software.upskilled.repository;

import com.software.upskilled.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}
