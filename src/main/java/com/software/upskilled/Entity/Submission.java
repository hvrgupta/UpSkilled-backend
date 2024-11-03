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
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String submissionUrl;  // URL or file path of the uploaded assignment

    @CreationTimestamp
    @Column(updatable = false, name = "submitted_at")
    private Date submittedAt;

    @Column
    private String status;  // "Submitted", "Graded", "Pending Review"

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Users employee;  // Employee who submitted the assignment

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL)
    private Gradebook grade;
}
