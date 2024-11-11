package com.software.upskilled.service;

import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.dto.FileDeletionResponse;
import com.software.upskilled.repository.CourseMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CourseMaterialService
{
    @Autowired
    CourseMaterialRepository courseMaterialRepository;

    public CourseMaterial saveCourseMaterial(CourseMaterial courseMaterial)
    {
        return courseMaterialRepository.save(courseMaterial);
    }

    public CourseMaterial getCourseMaterialByTitle( String courseMaterialTitle ){
        return courseMaterialRepository.findByTitle(courseMaterialTitle);
    }

    public CourseMaterial getCourseMaterialById(Long courseMaterialId){
        Optional<CourseMaterial> courseMaterial = courseMaterialRepository.findById(courseMaterialId);
        //Return the courseMaterial if the Course-Material is present else return null
        return courseMaterial.orElse(null);
    }

}
