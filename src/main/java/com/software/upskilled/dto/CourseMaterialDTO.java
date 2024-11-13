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
    private Long id;
    private String materialTitle;
    private String materialDescription;
}
