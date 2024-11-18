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
public class Course {
    /**
     * Entity class representing a Course.
     * This class maps to a table in the database where courses are stored,
     * each course having a title, description, name, instructor, and various associated materials, enrollments, announcements, assignments, and messages.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(length = 5000)
    private String syllabusUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    /**
     * The instructor associated with this course.
     * Each course is taught by a single instructor.
     * This field represents a many-to-one relationship with the Users entity.
     */
    @ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name = "instructor_id")
    private Users instructor;

    /**
     * One-to-many association of the course materials
     * between the course and the materials associated with it
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<CourseMaterial> courseMaterials;

    /**
     * A collection of enrollments in this course.
     * This represents a one-to-many relationship with the Enrollment entity.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Enrollment> enrollments;

    /**
     * A collection of announcements related to this course.
     * This represents a one-to-many relationship with the Announcement entity.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Announcement> announcements;

    /**
     * A collection of assignments for this course.
     * This represents a one-to-many relationship with the Assignment entity.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Assignment> assignments;

    /**
     * A collection of messages related to this course.
     * This represents a one-to-many relationship with the Message entity.
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private Set<Message> messages;

    /**
     * Enum representing the status of the course.
     * A course can be either ACTIVE or INACTIVE.
     */
    public enum Status {
        ACTIVE,
        INACTIVE
    }
}
