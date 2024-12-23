package com.SafeCryptoStocks.controller;

import com.SafeCryptoStocks.model.User;
import com.SafeCryptoStocks.services.EmailService;
import com.SafeCryptoStocks.services.JwtService;
import com.SafeCryptoStocks.services.UserServices;
import com.SafeCryptoStocks.utils.OtpUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceView;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServices userServices;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtService jwtService;

    @InjectMocks
    private HomeController homeController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");

            MockitoAnnotations.openMocks(this);
            mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                    .setViewResolvers((viewName, locale) -> {
                        // Mock ViewResolver: Resolves all views as valid
                        return new InternalResourceView("/WEB-INF/views/" + viewName + ".jsp");
                    })
                    .build();
    }

    // Test /login endpoint
    @Test
    void testLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

    }

    // Test /signup endpoint
    @Test
    void testSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("user"));
    }

    // Test /registerUser endpoint
    @Test
    void testRegisterUserSuccess() throws Exception {
        when(userServices.registerUser(any(User.class))).thenReturn(testUser);

        String userJson = """
                {
                  "email": "test@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful!"));
    }

    @Test
    void testRegisterUserEmailConflict() throws Exception {
        doThrow(new org.springframework.dao.DataIntegrityViolationException("Conflict")).when(userServices).registerUser(any(User.class));

        String userJson = """
                {
                  "email": "test@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/registerUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email or username already exists!"));
    }

    // Test /loginUser endpoint
    @Test
    void testLoginUserSuccess() throws Exception {
        when(userServices.validateUserLogin(anyString(), anyString())).thenReturn(true);
        when(userServices.findByEmail(anyString())).thenReturn(testUser);
        when(jwtService.generateToken(anyString())).thenReturn("mockToken");

        String userJson = """
                {
                  "email": "test@example.com",
                  "password": "password123"
                }
                """;

        mockMvc.perform(post("/loginUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent to your email. You can verify it to complete the login."))
                .andExpect(jsonPath("$.token").value("mockToken"));
    }

    @Test
    void testLoginUserInvalidCredentials() throws Exception {
        when(userServices.validateUserLogin(anyString(), anyString())).thenReturn(false);

        String userJson = """
                {
                  "email": "test@example.com",
                  "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/loginUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid credentials!"));
    }

    // Test /verifyOtp endpoint
    @Test
    void testVerifyOtpSuccess() throws Exception {
        // Set up the required OTP storage
        Map<String, String> otpStorage = new HashMap<>();
        otpStorage.put("test@example.com", "123456");
        ReflectionTestUtils.setField(homeController, "otpStorage", otpStorage);

        // Create a mock session
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userEmail", "test@example.com");

        // Mock the user returned from the database
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setUsername("testuser");
        mockUser.setFirstname("John");
        mockUser.setLastname("Doe");

        when(userServices.findByEmail("test@example.com")).thenReturn(mockUser);
        when(jwtService.generateToken("test@example.com")).thenReturn("mock-jwt-token");

        // Perform the request
        mockMvc.perform(post("/verifyOtp")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"otp\":\"123456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP verified. Login successful!"))
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        // Verify that OTP was removed after success
        otpStorage.clear();
    }


    @Test
    void testVerifyOtpInvalid() throws Exception {
        String otpData = """
                {
                  "email": "test@example.com",
                  "otp": "wrongOtp"
                }
                """;

        mockMvc.perform(post("/verifyOtp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otpData))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid OTP or email!"));
    }

    // Test /forgotPassword endpoint
    @Test
    void testForgotPasswordSuccess() throws Exception {
        when(userServices.checkEmail(anyString())).thenReturn(true);

        String requestJson = """
                {
                  "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/forgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset OTP sent to your email."));
    }

    @Test
    void testForgotPasswordEmailNotFound() throws Exception {
        when(userServices.checkEmail(anyString())).thenReturn(false);

        String requestJson = """
                {
                  "email": "test@example.com"
                }
                """;

        mockMvc.perform(post("/forgotPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("No user found with the provided email."));
    }

    // Test /resetPassword endpoint
    @Test
    void testResetPasswordSuccess() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userEmail", "test@example.com");

        String requestJson = """
                {
                  "newPassword": "newPassword123"
                }
                """;

        when(userServices.findByEmail(anyString())).thenReturn(testUser);

        mockMvc.perform(post("/resetPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successful."));
    }
}
