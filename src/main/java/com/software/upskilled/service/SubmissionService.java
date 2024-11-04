package com.software.upskilled.service;

import com.software.upskilled.Entity.Submission;
import com.software.upskilled.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmissionService
{
    @Autowired
    SubmissionRepository submissionRepository;

    public Submission saveSubmissionDetails( Submission submission )
    {
        return submissionRepository.save( submission );
    }

    public Submission getSubmissionByID( long submissionID )
    {
        return submissionRepository.getSubmissionById( submissionID );
    }

}
