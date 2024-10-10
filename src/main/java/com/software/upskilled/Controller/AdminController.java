package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.service.AppUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AppUserDetailsService appUserDetailsService;

    @PostMapping("/createUser")
    ResponseEntity<Long> createUser(@RequestBody Users appUser) {
        Long id = appUserDetailsService.createUser(appUser);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello Admin";
    }
}
