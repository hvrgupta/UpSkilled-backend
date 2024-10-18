package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.service.AssignmentService;
import com.software.upskilled.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/courses/{courseId}/assignments")
public class AssignmentController {
    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private CourseService courseService;

    // Get all assignments for a course
    @GetMapping
    public ResponseEntity<List<Assignment>> getAssignmentsByCourse(@PathVariable Long courseId) {
        List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
        return ResponseEntity.ok(assignments);
    }

    // View a specific assignment
    @GetMapping("/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long courseId,
                                                        @PathVariable Long assignmentId) {
        Assignment assignment = assignmentService.getAssignmentById(assignmentId);
        if(assignment == null) return ResponseEntity.badRequest().body("Invalid assignment ID");

        return ResponseEntity.ok(assignment);
    }


}
