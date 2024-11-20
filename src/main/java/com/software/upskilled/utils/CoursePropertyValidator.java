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

    /**
     * Checks whether a given property belongs to a specific course.
     *
     * This method validates if a property (such as a course material or assignment) is associated with the course identified
     * by the provided course ID. The property is specified through a map where the key is the type of property (e.g.,
     * "courseMaterial" or "assignment") and the value is the ID of the property. The method will check whether the given
     * property ID exists within the specified course's associated materials or assignments.
     *
     * @param courseId The ID of the course to validate the property against.
     * @param propertyValue A map containing the property type as the key and the property ID as the value.
     * @return `true` if the property exists in the specified course, otherwise `false`.
     */
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
