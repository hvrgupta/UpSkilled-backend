package com.software.upskilled.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.*;

@Table(name="users")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users implements UserDetails {
    /**
     * The Users class represents a user entity in the system.
     * It implements UserDetails to integrate with Spring Security for authentication and authorization.
     * Fields:
     * - id: Unique identifier for each user.
     * - email: The user's email, which is also the username.
     * - password: The user's password, stored securely.
     * - createdAt: The timestamp when the user was created.
     * - updatedAt: The timestamp of the last update to the user.
     * - role: The user's role (e.g., admin, instructor, employee).
     * - status: The current status of the user (ACTIVE, INACTIVE, or REJECTED).
     * - firstName: The user's first name.
     * - lastName: The user's last name.
     * - designation: The user's job title or role (optional).
     *
     * Relationships:
     * - coursesTaught: A set of courses taught by the user (if they are an instructor).
     * - enrollments: A set of courses the user is enrolled in (if they are an employee).
     * - submissions: A set of assignments submitted by the user (if they are an employee).
     * - gradesGiven: A set of grades given by the user (if they are an instructor).
     * - messagesSent: A set of messages sent by the user.
     * - messagesReceived: A set of messages received by the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "first_name", nullable = false)  // Specify column name and constraints
    private String firstName;       // User's first name

    @Column(name = "last_name", nullable = false)   // Specify column name and constraints
    private String lastName;        // User's last name

    @Column(name = "designation", length = 50)      // Specify column name and max length
    private String designation;     // User's designation

//  Course this user teaches
    @OneToMany(mappedBy = "instructor",fetch = FetchType.LAZY)
    private Set<Course> coursesTaught;

//  Enrollment where this user is an
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<Enrollment> enrollments;

//  Submissions by employee
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<Submission> submissions;

//   Grades given by instructor
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Set<Gradebook> gradesGiven;

//  Messages sent
    @OneToMany(mappedBy = "sender",fetch = FetchType.LAZY)
    private Set<Message> messagesSent;

//  Messages received

    @OneToMany(mappedBy = "recipient",fetch = FetchType.LAZY)
    private Set<Message> messagesReceived;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(role));
        return grantedAuthorities;
    }
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == Status.ACTIVE;
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        REJECTED
    }
}
