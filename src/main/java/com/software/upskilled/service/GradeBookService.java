package com.software.upskilled.service;

import com.software.upskilled.Entity.Gradebook;
import com.software.upskilled.repository.GradeBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GradeBookService
{
    @Autowired
    GradeBookRepository gradeBookRepository;

    public Gradebook saveGradeBookSubmission( Gradebook gradeBookSubmission )
    {
        return gradeBookRepository.save( gradeBookSubmission );
    }

}
