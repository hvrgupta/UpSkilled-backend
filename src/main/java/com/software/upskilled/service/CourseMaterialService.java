package com.software.upskilled.service;

import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.repository.CourseMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing course materials. Provides methods to save, update, delete,
 * retrieve materials by title, ID, and course, and delete all materials by course ID.
 */
@Service
public class CourseMaterialService
{
    @Autowired
    CourseMaterialRepository courseMaterialRepository;

    public CourseMaterial saveCourseMaterial(CourseMaterial courseMaterial)
    {
        return courseMaterialRepository.save(courseMaterial);
    }

    @Transactional
    @Modifying( flushAutomatically = true )
    public CourseMaterial updateCourseMaterial( CourseMaterial courseMaterial )
    {
        return courseMaterialRepository.save(courseMaterial);
    }

    @Transactional
    @Modifying( flushAutomatically = true )
    public void deleteCourseMaterial(Long courseMaterialId )
    {
        courseMaterialRepository.deleteCourseMaterialByCourseId( courseMaterialId );
    }

    public CourseMaterial getCourseMaterialByTitle( String courseMaterialTitle ){
        return courseMaterialRepository.findByTitle(courseMaterialTitle);
    }

    public CourseMaterial getCourseMaterialById(Long courseMaterialId){
        Optional<CourseMaterial> courseMaterial = courseMaterialRepository.findById(courseMaterialId);
        //Return the courseMaterial if the Course-Material is present else return null
        return courseMaterial.orElse(null);
    }

    public List<CourseMaterial> getAllCourseMaterialsByCourseId(Long courseId) {
        return courseMaterialRepository.findAllByCourseId(courseId);
    }

    @Transactional
    public void deleteCourseMaterialsByCourseId(Long courseId) {
        courseMaterialRepository.deleteByCourseId(courseId);
    }
}
