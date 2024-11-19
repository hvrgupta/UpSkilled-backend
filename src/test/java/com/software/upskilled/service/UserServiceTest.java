package com.software.upskilled.service;

import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest
{
    private UserRepository userRepository;
    private Users userDetails;

    @BeforeEach
    void setUp()
    {
        //Setup the mock object
        userRepository = Mockito.mock(UserRepository.class);
        userDetails = Mockito.mock(Users.class);
    }

    @AfterEach
    void tearDown()
    {
        userRepository = null;
        userDetails = null;
    }

    void test_loadEmployeeUserByUserName()
    {
        //Set up the stubbing details
        userDetails = new Users();
        userDetails.setId(1L);
        userDetails.setFirstName("John");
        userDetails.setLastName("Smith");
        userDetails.setDesignation("SDE");
        userDetails.setEmail("johnsmith@upskilled.com");
        userDetails.setPassword("john@123");
        userDetails.setRole("EMPLOYEE");
        userDetails.setStatus( Users.Status.ACTIVE );

        //Mock the userRepository to return userDetails when findByEmail is called
        when( userRepository.findByEmail("johnsmith@upskilled.com") ).thenReturn( userDetails );

        //Call the method under test
        UserDetails employeeDetails = userRepository.findByEmail( "johnsmith@upskilled.com" );

        //Assert the result
        assertNotNull(employeeDetails, "The returned user details should not be null");
        assertEquals("johnsmith@upskilled.com", employeeDetails.getUsername(), "Email should match");
        assertEquals("john@123", employeeDetails.getPassword(), "Password should match");
        assertTrue(employeeDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("EMPLOYEE")),
                "Role should include EMPLOYEE");

        // Verify mock interactions
        verify(userRepository).findByEmail("johnsmith@upskilled.com");
    }

    void test_loadInstructorUserByUserName()
    {
        //Set up the stubbing details
        userDetails = new Users();
        userDetails.setId(1L);
        userDetails.setFirstName("John");
        userDetails.setLastName("Doe");
        userDetails.setDesignation("Instructor");
        userDetails.setEmail("johndoe@upskilled.com");
        userDetails.setPassword("doe@123");
        userDetails.setRole("INSTRUCTOR");
        userDetails.setStatus( Users.Status.ACTIVE );

        //Mock the userRepository to return userDetails when findByEmail is called
        when( userRepository.findByEmail("johndoe@upskilled.com") ).thenReturn( userDetails );

        //Call the method under test
        UserDetails employeeDetails = userRepository.findByEmail( "johndoe@upskilled.com" );

        //Assert the result
        assertNotNull(employeeDetails, "The returned user details should not be null");
        assertEquals("johndoe@upskilled.com", employeeDetails.getUsername(), "Email should match");
        assertEquals("doe@123", employeeDetails.getPassword(), "Password should match");
        assertTrue(employeeDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("INSTRUCTOR")),
                "Role should include Instructor");

        // Verify mock interactions
        verify(userRepository).findByEmail("johndoe@upskilled.com");
    }


}
