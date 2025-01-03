package com.software.upskilled.dto;

import com.software.upskilled.Entity.Submission;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAssignmentGradeDTO {
    /**
     * Data Transfer Object (DTO) for employee assignment grading.
     * This DTO is used to transfer the grading information of an assignment submission for an employee.
     */
    private Long assignmentId;
    private String assignmentName;
    private Long submissionId;
    private int grade;
    private Submission.Status status;
}
