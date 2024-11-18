package com.software.upskilled.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coursematerial")
public class CourseMaterial
{
    /**
     * Entity class representing a CourseMaterial.
     * This class maps to a table in the database where course materials are stored,
     * associated with both the course and the instructor.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(length = 5000)
    private String courseMaterialUrl;

    @ManyToOne
    @JoinColumn(name = "instructor_id")
    private Users instructor;

    @ManyToOne
    @JoinColumn(name="course_id")
    private Course course;

}
