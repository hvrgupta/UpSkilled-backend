package com.software.upskilled.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {

    @Getter
    private Long id;

    private String title;
    private String content;
}
