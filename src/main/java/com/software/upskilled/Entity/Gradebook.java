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
public class Gradebook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer grade;  // Grade for the submission, e.g., 0-100

    @Column(length = 5000)
    private String feedback;  // Optional feedback for the submission

    @CreationTimestamp
    @Column(updatable = false, name = "graded_at")
    private Date gradedAt;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;  // Link to the assignment submission

    @ManyToOne
    @JoinColumn(name = "graded_by", nullable = false)
    private Users instructor;  // Instructor who graded the submission
}
