package com.software.upskilled.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AuthRequest;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.UserService;
import com.software.upskilled.utils.JWTUtil;
import com.software.upskilled.utils.TokenBlackListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userDetailsService;

    @MockBean
    private JWTUtil jwtUtil;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenBlackListService tokenBlackListService;


    @Test
    void testGetCurrentUser_Success() throws Exception {
        // Create a mock user object to simulate the authenticated user
        Users currentUser = Users.builder()
                .id(1L)
                .email("test@upskilled.com")
                .firstName("Test")
                .lastName("Test")
                .designation("Instructor")
                .role("INSTRUCTOR")
                .password("securePassword")
                .status(Users.Status.ACTIVE)
                .build();

        // Mock the SecurityContextHolder to simulate an authenticated user
        Authentication authentication = new UsernamePasswordAuthenticationToken(currentUser, null,
                AuthorityUtils.createAuthorityList("ROLE_INSTRUCTOR"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Perform the GET request to the /user endpoint
        mockMvc.perform(get("/api/auth/user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Check for 200 OK response
                .andExpect(jsonPath("$.id").value(1L)) // Check ID
                .andExpect(jsonPath("$.email").value("test@upskilled.com")) // Check email
                .andExpect(jsonPath("$.firstName").value("Test")) // Check first name
                .andExpect(jsonPath("$.lastName").value("Test")) // Check last name
                .andExpect(jsonPath("$.role").value("INSTRUCTOR")) // Check role
                .andExpect(jsonPath("$.password").value("*******")) // Check password (masked)
                .andExpect(jsonPath("$.status").value("ACTIVE")) // Check status
                .andExpect(jsonPath("$.designation").value("Instructor")); // Check designation

    }

    @Test
    void testRegisterUser_MissingFields() throws Exception {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setEmail("test@upskilled.com");
        userDTO.setPassword("securePassword");
        userDTO.setFirstName("Test");
        userDTO.setLastName("");
        userDTO.setRole("INSTRUCTOR");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email, First Name or Last Name missing!"));
    }

    @Test
    void testRegisterUser_Password() throws Exception {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setEmail("test@upskilled.com");
        userDTO.setPassword("secur");
        userDTO.setFirstName("Test");
        userDTO.setLastName("Test");
        userDTO.setRole("INSTRUCTOR");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password should be greater than 6"));
    }

    @Test
    void testRegisterUser_Role() throws Exception {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setEmail("test@upskilled.com");
        userDTO.setPassword("secure");
        userDTO.setFirstName("Test");
        userDTO.setLastName("Test");
        userDTO.setRole("NOT-A-ROLE");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Role specified is incorrect"));
    }

    @Test
    void testRegisterUser_Success() throws Exception {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setEmail("test@upskilled.com");
        userDTO.setPassword("securePassword");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setRole("INSTRUCTOR");

        doNothing().when(userDetailsService).createUser((Users) any(Users.class));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully!"));
    }
    @Test
    void testLogin_InvalidCredentials() throws Exception {
        // Prepare the invalid login request
        AuthRequest authRequest = new AuthRequest("test@upskilled.com", "wrongPassword");

        // Mock the authentication manager to throw BadCredentialsException when called
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password"));

        // Perform the login request
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                // Expect Unauthorized status (401)
                .andExpect(status().isUnauthorized())
                // Expect error message "Invalid username or password"
                .andExpect(content().string("Invalid username or password"));
    }


    @Test
    void testLogout_Success() throws Exception {
        String token = "sample-jwt-token";

        doNothing().when(tokenBlackListService).blacklistToken(token);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        Users user = Users.builder().email("test@upskilled.com").build();
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setPassword("newSecurePassword");
        userDTO.setDesignation("Updated Designation");

        when(userDetailsService.findUserByEmail(user.getEmail())).thenReturn(user);
        doNothing().when(userDetailsService).updateUser(any(Users.class));

        mockMvc.perform(post("/api/auth/update-profile")
                        .principal(new UsernamePasswordAuthenticationToken(user.getEmail(), null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User updated successfully!"));
    }

    @Test
    void testUpdateUser_UserNotFound() throws Exception {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setPassword("newSecurePassword");

        when(userDetailsService.findUserByEmail(anyString())).thenReturn(null);

        mockMvc.perform(post("/api/auth/update-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
            // Prepare the request and the mock response data
            AuthRequest authRequest = new AuthRequest("test@upskilled.com", "securePassword");

            // Create the expected user object
            Users user = Users.builder()
                    .email(authRequest.getEmail())
                    .firstName("Test")
                    .lastName("Test")
                    .designation("Test")
                    .role("INSTRUCTOR")
                    .password("securePassword")  // Make sure this is the same as in the request
                    .status(Users.Status.ACTIVE)
                    .build();

            // Mock the behavior of the AuthenticationManager to return an authenticated token
            Authentication authentication = new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword(), null);
            when(authenticationManager.authenticate(any(Authentication.class)))
                    .thenReturn(authentication);  // Mock successful authentication with the correct object

            // Mock userDetailsService to return the expected user
            when(userDetailsService.findUserByEmail(authRequest.getEmail())).thenReturn(user);

            // Mock the JWT generation utility to return a mock token
            String token = "sample-jwt-token";
            when(jwtUtil.generateToken(user)).thenReturn(token);  // Mock JWT generation

            // Perform the login request
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new ObjectMapper().writeValueAsString(authRequest)))
                    .andExpect(status().isOk())  // Expecting HTTP 200 OK
                    .andExpect(content().string(token));  // Expecting the mock JWT token in response

    }
}








