package com.software.upskilled.repository;

import com.software.upskilled.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing User entities. Provides methods to find users by email,
 * role, and status, as well as to delete users from the repository.
 */
@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
    List<Users> findByRole(String role);
    List<Users> findByRoleAndStatus(String role, Users.Status status);
    void delete(Users user);
}
