package com.software.upskilled.Controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/") // Handle requests to the root path
    public ResponseEntity<MyResponse> home() {
        MyResponse response = new MyResponse("Upskilled");
        return ResponseEntity.ok(response);
    }

    static class MyResponse {
        private String name;

        public MyResponse(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}