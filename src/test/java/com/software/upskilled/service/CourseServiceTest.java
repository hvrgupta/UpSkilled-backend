package com.software.upskilled.service;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.CourseRepository;
import com.software.upskilled.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CourseServiceTest
{
    private CourseRepository courseRepository;
    private Course courseDetails;
    private Users userDetails;
    private CourseService courseService;

    @BeforeEach
    void setUp()
    {
        //Setup the mock object
        courseRepository = Mockito.mock(CourseRepository.class);
        courseDetails = Mockito.mock(Course.class);
        userDetails = Mockito.mock( Users.class );
        courseService = Mockito.mock(CourseService.class);
    }

    @AfterEach
    void tearDown()
    {
        courseRepository = null;
        courseDetails = null;
        userDetails = null;
        courseService = null;
    }

    @Test
    void testSaveCourse()
    {
        //Set up the stubbing details for the instructor
        userDetails = new Users();
        userDetails.setId(1L);
        userDetails.setFirstName("John");
        userDetails.setLastName("Doe");
        userDetails.setDesignation("Instructor");
        userDetails.setEmail("johndoe@upskilled.com");
        userDetails.setPassword("doe@123");
        userDetails.setRole("INSTRUCTOR");
        userDetails.setStatus( Users.Status.ACTIVE );

        //Set up Stubbing Details for Course;
        Course course = new Course();
        course.setId(1L);
        course.setTitle("ENPM-613");
        course.setName("Software Design and Implementation");
        course.setDescription( "This is a sample course" );
        course.setStatus( Course.Status.ACTIVE );
        course.setInstructor( userDetails );
        course.setCourseMaterials( new HashSet<>());
        course.setEnrollments(new HashSet<>());
        course.setAnnouncements( new HashSet<>() );
        course.setAssignments( new HashSet<>() );
        course.setMessages( new HashSet<>() );

        //Set up the mockito behaviour
        when( courseService.saveCourse( course ) ).thenReturn( course );

        //Call the method under test
        Course savedCourse = courseService.saveCourse( course );

        //Verify all the assertions
        // Add assertions to verify the result
        assertNotNull(savedCourse, "The saved course should not be null");
        assertEquals(1L, savedCourse.getId(), "Course ID should be 1L");
        assertEquals("ENPM-613", savedCourse.getTitle(), "Course title should match");
        assertEquals("Software Design and Implementation", savedCourse.getName(), "Course name should match");
        assertEquals("This is a sample course", savedCourse.getDescription(), "Course description should match");
        assertEquals(Course.Status.ACTIVE, savedCourse.getStatus(), "Course status should be ACTIVE");
        assertNotNull(savedCourse.getInstructor(), "The instructor should not be null");
        assertEquals("John", savedCourse.getInstructor().getFirstName(), "Instructor's first name should match");
        assertEquals("Doe", savedCourse.getInstructor().getLastName(), "Instructor's last name should match");
        assertEquals("INSTRUCTOR", savedCourse.getInstructor().getRole(), "Instructor's role should match");
        assertTrue(savedCourse.getCourseMaterials().isEmpty(), "Course materials should be empty");
        assertTrue(savedCourse.getEnrollments().isEmpty(), "Enrollments should be empty");
        assertTrue(savedCourse.getAnnouncements().isEmpty(), "Announcements should be empty");
        assertTrue(savedCourse.getAssignments().isEmpty(), "Assignments should be empty");
        assertTrue(savedCourse.getMessages().isEmpty(), "Messages should be empty");

        // Verify the interaction with the mock
        verify(courseService).saveCourse(course);

    }

    @Test
    void testFindCourseById() {
        // Set up the course details
        Course course = new Course();
        course.setId(1L);
        course.setTitle("ENPM-613");
        course.setName("Software Design and Implementation");

        // Set up Mockito behavior
        when(courseService.findCourseById(1L)).thenReturn(course);

        // Call the method under test
        Course foundCourse = courseService.findCourseById(1L);

        // Verify assertions
        assertNotNull(foundCourse, "The found course should not be null");
        assertEquals(1L, foundCourse.getId(), "Course ID should match");
        assertEquals("ENPM-613", foundCourse.getTitle(), "Course title should match");
        assertEquals("Software Design and Implementation", foundCourse.getName(), "Course name should match");

        // Verify interaction with the mock
        verify(courseService).findCourseById(1L);
    }


    @Test
    void testFindByTitle() {
        // Set up the course details
        Course course = new Course();
        course.setId(1L);
        course.setTitle("ENPM-613");
        course.setName("Software Design and Implementation");

        //Set up the behaviour of the spy object
        doReturn( course ).when( courseRepository ).findByTitle("ENPM-613");

        //Set up the method of the service class
        when( courseService.findByTitle( "ENPM-613" ) ).thenReturn( course );

        // Call the method under test
        Course foundCourse = courseService.findByTitle("ENPM-613");

        // Verify assertions
        assertNotNull(foundCourse, "The found course should not be null");
        assertEquals("ENPM-613", foundCourse.getTitle(), "Course title should match");
        assertEquals("Software Design and Implementation", foundCourse.getName(), "Course name should match");

        // Verify interaction with the mock
        verify(courseService).findByTitle("ENPM-613");
    }

    @Test
    void testGetAllCourses() {
        // Set up the course list
        Course course1 = new Course();
        course1.setId(1L);
        course1.setTitle("ENPM-613");
        course1.setName("Software Design and Implementation");

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("ENPM-614");
        course2.setName("Advanced Software Design");

        List<Course> courseList = Arrays.asList(course1, course2);

        // Set up Mockito behavior
        when(courseService.getAllCourses()).thenReturn(courseList);

        // Call the method under test
        List<Course> allCourses = courseService.getAllCourses();

        // Verify assertions
        assertNotNull(allCourses, "The list of courses should not be null");
        assertEquals(2, allCourses.size(), "The list size should be 2");
        assertEquals("ENPM-613", allCourses.get(0).getTitle(), "First course title should match");
        assertEquals("ENPM-614", allCourses.get(1).getTitle(), "Second course title should match");

        // Verify interaction with the mock
        verify(courseService).getAllCourses();
    }

    @Test
    void testFindByInstructorId() {
        // Set up the courses for the instructor
        Course course1 = new Course();
        course1.setId(1L);
        course1.setTitle("ENPM-613");
        course1.setName("Software Design and Implementation");

        Course course2 = new Course();
        course2.setId(2L);
        course2.setTitle("ENPM-614");
        course2.setName("Advanced Software Design");

        List<Course> coursesByInstructor = Arrays.asList(course1, course2);

        //Set up Mockito behavior for the repository
        doReturn( coursesByInstructor ).when( courseRepository ).findByInstructorId(1L);
        // Set up Mockito behavior
        when(courseService.findByInstructorId(1L)).thenReturn( coursesByInstructor );

        // Call the method under test
        List<Course> foundCourses = courseService.findByInstructorId(1L);

        // Verify assertions
        assertNotNull(foundCourses, "The list of courses should not be null");
        assertEquals(2, foundCourses.size(), "The list size should be 2");
        assertEquals("ENPM-613", foundCourses.get(0).getTitle(), "First course title should match");
        assertEquals("ENPM-614", foundCourses.get(1).getTitle(), "Second course title should match");

        // Verify interaction with the mock
        verify(courseService).findByInstructorId(1L);
    }


}
