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
public class Users implements UserDetails {

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
    @OneToMany(mappedBy = "instructor")
    private Set<Course> coursesTaught;

//  Enrollment where this user is an
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private Set<Enrollment> enrollments;

//  Submissions by employee
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private Set<Submission> submissions;

//   Grades given by instructor
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL)
    private Set<Gradebook> gradesGiven;

//  Messages sent
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private Set<Message> messagesSent;

//  Messages received
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
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
        return true;
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        REJECTED
    }
}
