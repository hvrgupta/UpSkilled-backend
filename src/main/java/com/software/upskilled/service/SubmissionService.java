package com.software.upskilled.service;

import com.software.upskilled.Entity.Submission;
import com.software.upskilled.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for handling submission operations. Provides methods to save, modify, retrieve, and sort submissions based on submission time.
 * Includes functionality for managing individual submission details and retrieving submissions for assignments.
 */

@Service
public class SubmissionService
{
    @Autowired
    SubmissionRepository submissionRepository;

    public Submission saveSubmissionDetails( Submission submission )
    {
        return submissionRepository.save( submission );
    }

    @Transactional
    @Modifying
    public Submission modifySubmissionDetails( Submission submission ) {
        return submissionRepository.save( submission );
    }

    public Submission getSubmissionByID( long submissionID ) {
        return submissionRepository.getSubmissionById( submissionID );
    }

    public List<Submission> getSubmissionsSortedBySubmittedTime( Long assignmentId ) {
        return submissionRepository.getSubmissionsSortedBySubmissionTime( assignmentId );
    }
}
