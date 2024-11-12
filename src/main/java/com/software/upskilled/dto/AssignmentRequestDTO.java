package com.software.upskilled.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class AssignmentRequestDTO
{
    String title;
    String description;
    long deadline;
}
