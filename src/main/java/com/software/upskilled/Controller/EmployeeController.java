package com.software.upskilled.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    @GetMapping("/hello")
    public String hello(){
        return "Hello Employee";
    }
}
