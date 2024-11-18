package com.software.upskilled.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CourseDTO;
import com.software.upskilled.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private FileService fileService;

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private AnnouncementService announcementService;

    @MockBean
    private CourseMaterialService courseMaterialService;

    @Test
    public void testGetInstructorsList() throws Exception {

        // Mocking inactive instructors
        Users inactiveInstructor = new Users();
        inactiveInstructor.setId(1L);
        inactiveInstructor.setEmail("inactive@example.com");
        inactiveInstructor.setRole("INSTRUCTOR");
        inactiveInstructor.setFirstName("Inactive");
        inactiveInstructor.setLastName("Instructor");
        inactiveInstructor.setDesignation("Math");
        inactiveInstructor.setStatus(Users.Status.INACTIVE);
        inactiveInstructor.setPassword("inactive@123");

        // Mocking active instructors
        Users activeInstructor = new Users();
        activeInstructor.setId(2L);
        activeInstructor.setEmail("active@example.com");
        activeInstructor.setRole("INSTRUCTOR");
        activeInstructor.setFirstName("Active");
        activeInstructor.setLastName("Instructor");
        activeInstructor.setDesignation("Science");
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setPassword("active@123");

        // Mock the userService to return the mock data

        when(userService.getInactiveInstructors()).thenReturn(List.of(inactiveInstructor));
        when(userService.getActiveInstructors()).thenReturn(List.of(activeInstructor));


        // Perform the GET request and verify the results
        mockMvc.perform(get("/api/admin/listInstructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(inactiveInstructor.getId()))
                .andExpect(jsonPath("$[0].email").value(inactiveInstructor.getEmail()))
                .andExpect(jsonPath("$[0].status").value(inactiveInstructor.getStatus().toString()))
                .andExpect(jsonPath("$[1].id").value(activeInstructor.getId()))
                .andExpect(jsonPath("$[1].email").value(activeInstructor.getEmail()))
                .andExpect(jsonPath("$[1].status").value(activeInstructor.getStatus().toString()))
                .andDo(print());
    }

    @Test
    public void testGetActiveInstructorsList() throws Exception {
        // Arrange: Create active and inactive instructors for testing
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail("active@example.com");
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("Active");
        activeInstructor.setLastName("Instructor");
        activeInstructor.setRole("INSTRUCTOR");

        Users inactiveInstructor = new Users();
        inactiveInstructor.setId(2L);
        inactiveInstructor.setEmail("inactive@example.com");
        inactiveInstructor.setStatus(Users.Status.INACTIVE);
        inactiveInstructor.setFirstName("Inactive");
        inactiveInstructor.setLastName("Instructor");
        inactiveInstructor.setRole("INSTRUCTOR");

        // Mock the service to return a list with active and inactive instructors
        when(userService.getActiveInstructors()).thenReturn(Arrays.asList(activeInstructor));

        // Act & Assert: Perform the request and verify the result
        mockMvc.perform(get("/api/admin/listActiveInstructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // Ensure only one instructor is returned (active)
                .andExpect(jsonPath("$[0].id").value(activeInstructor.getId()))
                .andExpect(jsonPath("$[0].email").value(activeInstructor.getEmail()))
                .andExpect(jsonPath("$[0].status").value(activeInstructor.getStatus().name())) // Verify the status is "ACTIVE"
                .andExpect(jsonPath("$[0].firstName").value(activeInstructor.getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(activeInstructor.getLastName()))
                .andExpect(jsonPath("$[0].role").value(activeInstructor.getRole()))
                .andExpect(jsonPath("$[0].designation").value(activeInstructor.getDesignation()))
                .andDo(print());

        // Verify that the service method was called
        verify(userService).getActiveInstructors();
    }

    @Test
    void testGetInstructorsList_OnlyActiveInstructors() throws Exception {
        // Arrange
        List<Users> activeInstructors = List.of(
                Users.builder()
                        .id(1L)
                        .email("active1@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .role("INSTRUCTOR")
                        .designation("Senior Instructor")
                        .status(Users.Status.ACTIVE)
                        .build(),
                Users.builder()
                        .id(2L)
                        .email("active2@example.com")
                        .firstName("Jane")
                        .lastName("Smith")
                        .role("INSTRUCTOR")
                        .designation("Instructor")
                        .status(Users.Status.ACTIVE)
                        .build()
        );

        when(userService.getInactiveInstructors()).thenReturn(Collections.emptyList());
        when(userService.getActiveInstructors()).thenReturn(activeInstructors);

        // Act & Assert
        mockMvc.perform(get("/api/admin/listInstructors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(activeInstructors.size()))
                .andExpect(jsonPath("$[0].id").value(activeInstructors.get(0).getId()))
                .andExpect(jsonPath("$[0].email").value(activeInstructors.get(0).getEmail()))
                .andExpect(jsonPath("$[0].firstName").value(activeInstructors.get(0).getFirstName()))
                .andExpect(jsonPath("$[0].lastName").value(activeInstructors.get(0).getLastName()))
                .andExpect(jsonPath("$[0].role").value(activeInstructors.get(0).getRole()))
                .andExpect(jsonPath("$[0].status").value(activeInstructors.get(0).getStatus().toString()))
                .andExpect(jsonPath("$[1].id").value(activeInstructors.get(1).getId()))
                .andExpect(jsonPath("$[1].email").value(activeInstructors.get(1).getEmail()))
                .andExpect(jsonPath("$[1].firstName").value(activeInstructors.get(1).getFirstName()))
                .andExpect(jsonPath("$[1].lastName").value(activeInstructors.get(1).getLastName()))
                .andExpect(jsonPath("$[1].role").value(activeInstructors.get(1).getRole()))
                .andExpect(jsonPath("$[1].status").value(activeInstructors.get(1).getStatus().toString()));

        // Verify interactions
        verify(userService).getInactiveInstructors();
        verify(userService).getActiveInstructors();
    }



    @Test
    public void testApproveInstructor() throws Exception {
        // Arrange: Create a mock instructor object
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.INACTIVE); // Initial status is INACTIVE

        // Mock the userService to return the instructor
        when(userService.findUserById(1L)).thenReturn(instructor);
        when(userService.saveUser(any(Users.class))).thenReturn(instructor);

        // Act & Assert: Perform the request and verify the result
        mockMvc.perform(post("/api/admin/approve/{instructorId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("User Approved!"))
                .andDo(print());

        // Verify that the instructor's status has been updated to ACTIVE
        verify(userService).saveUser(instructor);
        Assertions.assertEquals(Users.Status.ACTIVE, instructor.getStatus());
    }

    @Test
    public void testRejectInstructor() throws Exception {
        // Arrange: Create a mock instructor object
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.INACTIVE); // Initial status is INACTIVE

        // Mock the userService to return the instructor
        when(userService.findUserById(1L)).thenReturn(instructor);
        when(userService.saveUser(any(Users.class))).thenReturn(instructor);

        // Act & Assert: Perform the request and verify the result
        mockMvc.perform(post("/api/admin/reject/{instructorId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("User Rejected!"))
                .andDo(print());

        // Verify that the instructor's status has been updated to REJECTED
        verify(userService).saveUser(instructor);
        Assertions.assertEquals(Users.Status.REJECTED, instructor.getStatus());
    }

    @Test
    void testApproveInstructor_InvalidInstructorId() throws Exception {
        // Arrange
        Long invalidInstructorId = 99L; // An ID that does not exist
        when(userService.findUserById(invalidInstructorId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/admin/approve/" + invalidInstructorId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid instructor ID"));

        // Verify interactions
        verify(userService).findUserById(invalidInstructorId);
        verify(userService, never()).saveUser(any(Users.class));
    }

    @Test
    void testRejectInstructor_InvalidInstructorId() throws Exception {
        // Arrange
        Long invalidInstructorId = 99L; // An ID that does not exist
        when(userService.findUserById(invalidInstructorId)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/api/admin/reject/" + invalidInstructorId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid instructor ID"));

        // Verify interactions
        verify(userService).findUserById(invalidInstructorId);
        verify(userService, never()).saveUser(any(Users.class));
    }

    @Test
    public void testCreateNewCourse() throws Exception {
        // Arrange: Mock the instructor and courseDTO
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.ACTIVE); // Instructor is active

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setInstructorId(1L);
        courseDTO.setTitle("Java 101");
        courseDTO.setDescription("Basic Java course");
        courseDTO.setName("Java Course");

        when(userService.findUserById(1L)).thenReturn(instructor);
        when(courseService.findByTitle("Java 101")).thenReturn(null);  // No course with the title "Java 101"

        // Act & Assert: Perform the POST request and verify the result
        mockMvc.perform(post("/api/admin/createCourse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(courseDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Course created successfully for instructor: " + instructor.getEmail()))
                .andDo(print());

        // Verify that the course is saved
        verify(courseService).saveCourse(any(Course.class));
    }

    @Test
    void testCreateNewCourse_TitleAlreadyExists() throws Exception {
        // Arrange
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setInstructorId(1L);
        courseDTO.setTitle("Existing Course Title");
        courseDTO.setDescription("Course Description");
        courseDTO.setName("Course Name");

        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.ACTIVE);

        Course existingCourse = new Course();
        existingCourse.setTitle("Existing Course Title");

        when(userService.findUserById(courseDTO.getInstructorId())).thenReturn(instructor);
        when(courseService.findByTitle(courseDTO.getTitle())).thenReturn(existingCourse);

        // Act & Assert
        mockMvc.perform(post("/api/admin/createCourse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Course title already exists."));

        // Verify interactions
        verify(userService).findUserById(courseDTO.getInstructorId());
        verify(courseService).findByTitle(courseDTO.getTitle());
        verify(courseService, never()).saveCourse(any(Course.class));
    }


    @Test
    public void testCreateNewCourseInvalidInstructor() throws Exception {
        // Arrange: Mock an invalid instructor (not found or inactive)
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setInstructorId(1L);
        courseDTO.setTitle("Java 101");
        courseDTO.setDescription("Basic Java course");
        courseDTO.setName("Java Course");

        when(userService.findUserById(1L)).thenReturn(null);  // Invalid instructor

        // Act & Assert: Perform the POST request and verify the result
        mockMvc.perform(post("/api/admin/createCourse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid instructor ID"))
                .andDo(print());

        // Verify no course is saved
        verify(courseService, times(0)).saveCourse(any(Course.class));
    }

    @Test
    public void testCreateNewCourseMissingFields() throws Exception {
        // Arrange: Mock the instructor and courseDTO with missing title
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.ACTIVE); // Instructor is active

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setInstructorId(1L);
        courseDTO.setTitle("");  // Missing title
        courseDTO.setDescription("Basic Java course");
        courseDTO.setName("Java Course");

        when(userService.findUserById(1L)).thenReturn(instructor);
        when(courseService.findByTitle("")).thenReturn(null);  // No course with empty title

        // Act & Assert: Perform the POST request and verify the result
        mockMvc.perform(post("/api/admin/createCourse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Title, description or name missing!"))
                .andDo(print());

        // Verify no course is saved
        verify(courseService, times(0)).saveCourse(any(Course.class));
    }

    @Test
    public void testInactivateCourseValid() throws Exception {
        // Arrange: Mock a valid course
        when(courseService.findCourseById(1L)).thenReturn(null);

        // Act & Assert: Try inactivating the course and verify the response
        mockMvc.perform(post("/api/admin/course/inactivate/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Course not found."))
                .andDo(print());

        // Verify no course is updated
        verify(courseService, times(0)).saveCourse(any(Course.class));
    }


    @Test
    public void testInactivateCourseNotFound() throws Exception {
        // Arrange: Mock course not found
        when(courseService.findCourseById(1L)).thenReturn(null);

        // Act & Assert: Try inactivating the course and verify the response
        mockMvc.perform(post("/api/admin/course/inactivate/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Course not found."))
                .andDo(print());

        // Verify no course is updated
        verify(courseService, times(0)).saveCourse(any(Course.class));
    }

    @Test
    public void testInactivateCourse_Success() throws Exception {
        // Arrange
        Long courseId = 1L;
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail("instructor@upskilled.com");
        instructor.setStatus(Users.Status.ACTIVE);
        Course course = new Course();
        course.setId(courseId);
        course.setStatus(Course.Status.ACTIVE);
        course.setTitle("New title");
        course.setInstructor(instructor);

        when(courseService.findCourseById(courseId)).thenReturn(course);

        // Act & Assert
        mockMvc.perform(post("/api/admin/course/inactivate/{courseId}", courseId))
                .andExpect(status().isOk())
                .andExpect(content().string("Course Inactivated!"));

        // Verify
        verify(courseService, times(1)).findCourseById(courseId);
        verify(courseService, times(1)).saveCourse(any(Course.class));
        Assertions.assertEquals(Course.Status.INACTIVE, course.getStatus());
    }

    @Test
    public void testViewActiveCourses() throws Exception {
        // Arrange: Mock some active courses
        Course activeCourse = new Course();
        activeCourse.setId(1L);
        activeCourse.setStatus(Course.Status.ACTIVE);
        activeCourse.setTitle("Java Basics");
        activeCourse.setInstructor(new Users());
        activeCourse.getInstructor().setFirstName("John");
        activeCourse.getInstructor().setLastName("Doe");

        when(courseService.getAllCourses()).thenReturn(List.of(activeCourse));

        // Act & Assert: View active courses and verify the response
        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Java Basics"))
                .andDo(print());
    }

    @Test
    public void testViewActiveCoursesNone() throws Exception {
        // Arrange: Mock no active courses
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

        // Act & Assert: View active courses and verify the response
        mockMvc.perform(get("/api/admin/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andDo(print());
    }


    @Test
    public void testGetCourseDetailsValid() throws Exception {
        // Arrange: Mock a valid course
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Java Basics");
        course.setDescription("An introductory Java course");
        Users instructor = new Users();
        instructor.setFirstName("John");
        instructor.setLastName("Doe");
        course.setInstructor(instructor);
        when(courseService.findCourseById(1L)).thenReturn(course);

        // Act & Assert: Get course details and verify the response
        mockMvc.perform(get("/api/admin/course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Basics"))
                .andExpect(jsonPath("$.instructorName").value("John Doe"))
                .andDo(print());
    }


    @Test
    public void testGetCourseDetailsNotFound() throws Exception {
        // Arrange: Mock course not found
        when(courseService.findCourseById(1L)).thenReturn(null);

        // Act & Assert: Try getting course details and verify the response
        mockMvc.perform(get("/api/admin/course/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid course ID"))
                .andDo(print());
    }

    @Test
    public void testViewSyllabusValid() throws Exception {
        // Arrange: Mock a valid course with syllabus URL
        Course course = new Course();
        course.setId(1L);
        course.setSyllabusUrl("syllabus.pdf");
        when(courseService.findCourseById(1L)).thenReturn(course);
        when(fileService.viewSyllabus(1L)).thenReturn("syllabus data".getBytes());

        // Act & Assert: View syllabus and verify the response
        mockMvc.perform(get("/api/admin/1/syllabus"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition", "attachment; filename=\"syllabus.pdf\""))
                .andExpect(content().bytes("syllabus data".getBytes()))
                .andDo(print());
    }


    @Test
    public void testViewSyllabusNotUploaded() throws Exception {
        // Arrange: Mock course with no syllabus URL
        Course course = new Course();
        course.setId(1L);
        course.setSyllabusUrl(null);
        when(courseService.findCourseById(1L)).thenReturn(course);

        // Act & Assert: Try viewing syllabus and verify the response
        mockMvc.perform(get("/api/admin/1/syllabus"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No syllabus uploaded for this course."))
                .andDo(print());
    }

    @Test
    public void testViewSyllabusCourseNotFound() throws Exception {
        // Arrange: Mock course not found
        when(courseService.findCourseById(1L)).thenReturn(null);

        // Act & Assert: Try viewing syllabus and verify the response
        mockMvc.perform(get("/api/admin/1/syllabus"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid course ID"))
                .andDo(print());
    }

    @Test
    void testModifyCourseDetails_CourseDoesNotExist() throws Exception {
        Long courseId = 1L;
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("New Course Title");

        when(courseService.findCourseById(courseId)).thenReturn(null);

        mockMvc.perform(put("/api/admin/updateCourseDetails/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Course doesn't exist with the particular courseID"));
    }

    @Test
    void testModifyCourseDetails_DuplicateCourseTitle() throws Exception {
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Existing Title");

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("Duplicate Title");

        when(courseService.findCourseById(courseId)).thenReturn(existingCourse);
        when(courseService.findByTitle("Duplicate Title")).thenReturn(new Course());

        mockMvc.perform(put("/api/admin/updateCourseDetails/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Course title already exists."));
    }

    @Test
    void testModifyCourseDetails_InstructorDoesNotExist() throws Exception {
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Existing Title");
        existingCourse.setDescription("Existin desc");

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("Updated Title");
        courseDTO.setInstructorId(2L);
        courseDTO.setDescription("new desc");
        courseDTO.setName("updated name");

        when(courseService.findCourseById(courseId)).thenReturn(existingCourse);
        when(userService.findUserById(2L)).thenReturn(null);

        mockMvc.perform(put("/api/admin/updateCourseDetails/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Instructor does not exists."));
    }

    @Test
    void testModifyCourseDetails_UpdateWithSameInstructor() throws Exception {
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Existing Title");
        Users existingInstructor = new Users();
        existingInstructor.setId(2L);
        existingInstructor.setStatus(Users.Status.ACTIVE);
        existingCourse.setInstructor(existingInstructor);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("Updated Title");
        courseDTO.setInstructorId(2L);
        courseDTO.setDescription("new desc");
        courseDTO.setName("updated name");

        when(courseService.findCourseById(courseId)).thenReturn(existingCourse);
        when(courseService.findByTitle("Updated Title")).thenReturn(null);
        when(userService.findUserById(2L)).thenReturn(existingInstructor);

        mockMvc.perform(put("/api/admin/updateCourseDetails/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(courseDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Course Details updated successfully"));

        verify(courseService, times(1)).saveCourse(any(Course.class));
    }

    @Test
    void testModifyCourseDetails_UpdateWithNewInstructor() throws Exception {
        Long courseId = 1L;
        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Existing Title");
        Users existingInstructor = new Users();
        existingInstructor.setId(2L);
        existingInstructor.setStatus(Users.Status.ACTIVE);
        existingCourse.setInstructor(existingInstructor);

        Users newInstructor = new Users();
        newInstructor.setId(3L);
        newInstructor.setStatus(Users.Status.ACTIVE);

        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setTitle("Updated Title");
        courseDTO.setInstructorId(3L);
        courseDTO.setDescription("new desc");
        courseDTO.setName("updated name");

        when(courseService.findCourseById(courseId)).thenReturn(existingCourse);
        when(courseService.findByTitle("Updated Title")).thenReturn(null);
        when(userService.findUserById(3L)).thenReturn(newInstructor);

        mockMvc.perform(put("/api/admin/updateCourseDetails/{courseId}", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(courseDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Course Details updated successfully"));

        verify(assignmentService, times(1)).deleteAssignmentsByCourseId(courseId);
        verify(announcementService, times(1)).deleteAnnouncementsByCourseId(courseId);
        verify(courseMaterialService, times(1)).deleteCourseMaterialsByCourseId(courseId);
        verify(courseService, times(1)).saveCourse(any(Course.class));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
