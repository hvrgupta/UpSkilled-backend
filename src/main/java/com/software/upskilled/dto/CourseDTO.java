package com.software.upskilled.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDTO {
    /**
     * Data Transfer Object (DTO) for representing the details of a course.
     * This DTO is used to transfer data about a course, such as title, description, instructor, and course name.
     */
    private String title;
    private String description;
    private Long instructorId;
    private String name;
}
