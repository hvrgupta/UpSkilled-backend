package com.software.upskilled.utils;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Submission;
import com.software.upskilled.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component that provides validation for assignments and submissions.
 * The `validateSubmissionAgainstAssignment` method checks if a given submission ID exists for a specific assignment ID.
 * It fetches the assignment details and verifies if the submission ID is associated with that assignment.
 * Returns true if the submission exists for the assignment, otherwise returns false.
 */
@Component
public class AssignmentPropertyValidator
{
    @Autowired
    AssignmentService assignmentService;

    /**
     * Validates if a given submission exists for a specific assignment.
     *
     * This method checks whether the specified submission ID is associated with the given assignment ID.
     * It first retrieves the assignment details and verifies if the submission ID exists within the assignment's submissions.
     * If a matching submission is found, the method returns `true`, indicating that the submission is valid for the assignment.
     * Otherwise, it returns `false`.
     *
     * @param assignmentID The ID of the assignment to validate against.
     * @param submissionID The ID of the submission to validate.
     * @return `true` if the submission exists for the specified assignment, otherwise `false`.
     */
    public boolean validateSubmissionAgainstAssignment( long assignmentID, long submissionID ) {
        Assignment assignmentDetails = assignmentService.getAssignmentById( assignmentID );
        //Check if the assignmentDetails is null
        if( assignmentDetails == null )
            return false;
        else
        {
            //Check if this submissionID is present in this course
            List<Submission> submissionDetails = assignmentDetails.getSubmissions().stream().filter(
                    submission -> submission.getId() == submissionID
            ).toList();
            //If there exist a submission, then this means that the particular submission exists for the assignment
            return submissionDetails.size() == 1;
        }
    }
}
