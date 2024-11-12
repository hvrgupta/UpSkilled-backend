package com.software.upskilled.service;

import com.software.upskilled.Entity.Gradebook;
import com.software.upskilled.repository.GradeBookRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GradeBookService
{
    @Autowired
    GradeBookRepository gradeBookRepository;


    public Gradebook saveGradeBookSubmission( Gradebook gradeBookSubmission )
    {
        return gradeBookRepository.save( gradeBookSubmission );
    }

    @Transactional
    public void deleteGradeBookSubmission( long gradeBookSubmissionId )
    {
        //Deleting the grade linked
        gradeBookRepository.deleteById( gradeBookSubmissionId );
    }

    public Gradebook getGradeBookByID( long gradeBookSubmissionId )
    {
        Optional<Gradebook> gradeBook = gradeBookRepository.findById( gradeBookSubmissionId );
        return gradeBook.orElse(null);

    }



}
