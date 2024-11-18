package com.software.upskilled.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class CourseMaterialDTO
{
    /**
     * Data Transfer Object (DTO) for representing the material related to a course.
     * This DTO is used to transfer information about a specific material, including its title and description.
     */
    private Long id;
    private String materialTitle;
    private String materialDescription;
}
