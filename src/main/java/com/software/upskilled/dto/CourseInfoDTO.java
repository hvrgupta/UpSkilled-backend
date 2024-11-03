package com.software.upskilled.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseInfoDTO {
    private Long id;
    private String title;
    private String description;
    private String instructorName;
    private Long instructorId;
}
