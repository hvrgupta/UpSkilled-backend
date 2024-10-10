package com.software.upskilled.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/instructor")
public class InstructorController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello Instructor";
    }
}
