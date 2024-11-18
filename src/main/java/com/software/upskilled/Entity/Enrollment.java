package com.software.upskilled.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {
    /**
     * Entity class representing an Enrollment.
     * This class maps to a table in the database where the association between
     * a course and an employee (user) is stored, including the enrollment date.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Users employee;

    @CreationTimestamp
    @Column(name = "enrollment_date", updatable = false)
    private Date enrollmentDate;
}
