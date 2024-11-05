package com.software.upskilled.repository;

import com.software.upskilled.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
    List<Users> findByRole(String role);
    List<Users> findByRoleAndStatus(String role, Users.Status status);
    void delete(Users user);
}
