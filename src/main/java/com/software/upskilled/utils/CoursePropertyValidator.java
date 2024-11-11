package com.software.upskilled.utils;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A util method that validates whether the sub-property of the course such
 * as assignments, announcements, messages, submissions are actually of that course
 */
@Component
public class CoursePropertyValidator
{
    @Autowired
    CourseService courseService;

    public boolean isPropertyOfTheCourse(long courseId , Map<String,Long> propertyValue)
    {
        //Fetch the corresponding course related to the courseID;
        Course course = courseService.findCourseById( courseId );
        if( course == null )
            return false;
        //Extract the propertyName that is passed in the Map as the key
        String propertyType = propertyValue.keySet().stream().toList().get(0);
        boolean isPropertyOfTheCourse = false;
        switch( propertyType )
        {
            case "courseMaterial":
                //Get the value of the property
                long courseMaterialId = propertyValue.get( propertyType );
                //Check if the courseMaterial belongs to the list of CourseMaterial associated with that ID
                List<CourseMaterial> courseMaterial = course.getCourseMaterials().stream()
                        .filter( cm -> cm.getId() == courseMaterialId )
                        .toList();
                isPropertyOfTheCourse = courseMaterial.size() == 1;
                break;

            case "assignment":
                long assignmentID = propertyValue.get( propertyType );
                //Check if the assignment is the property of the Course
                List<Assignment> assignmentObject = course.getAssignments().stream()
                        .filter( assignment -> assignment.getId() == assignmentID  )
                        .toList();
                isPropertyOfTheCourse = assignmentObject.size() == 1;
                break;
            default:
                System.out.println("Invalid property type");
                break;
        }
        return isPropertyOfTheCourse;
    }
}
