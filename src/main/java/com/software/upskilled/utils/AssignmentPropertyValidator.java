package com.software.upskilled.utils;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Submission;
import com.software.upskilled.service.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssignmentPropertyValidator
{
    @Autowired
    AssignmentService assignmentService;

    public boolean validateSubmissionAgainstAssignment( long assignmentID, long submissionID )
    {
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
