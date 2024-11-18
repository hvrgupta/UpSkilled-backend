package com.software.upskilled.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment {
    /**
     * Entity class representing an Assignment.
     * This class maps to a table in the database where assignments related
     * to courses are stored. Each assignment is associated with a course
     * and has a creator (user).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false ,length = 10000)
    private String description;

    @Column(nullable = false)
    private Long deadline;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private Users createdBy;

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    private Set<Submission> submissions;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}
