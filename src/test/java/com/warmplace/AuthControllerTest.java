package com.warmplace;

import com.warmplace.dto.*;
import com.warmplace.entity.User;
import com.warmplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @DisplayName("회원가입 성공")
    @Test
    void signup_Success() throws Exception {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setNickname("테스트유저");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "email": "test@example.com",
                                "password": "password123",
                                "nickname": "테스트유저"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"))
                .andExpect(jsonPath("$.token").exists());

        assertTrue(userRepository.findByUsername("testuser").isPresent());
    }

    @DisplayName("중복用户名으로 회원가입 실패")
    @Test
    void signup_DuplicateUsername_Fails() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("existing@example.com");
        user.setPassword("password123");
        user.setNickname("기존유저");
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "email": "new@example.com",
                                "password": "password123",
                                "nickname": "새유저"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("로그인 성공")
    @Test
    void login_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setNickname("테스트유저");
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "password": "password123"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.token").exists());
    }

    @DisplayName("잘못된 비밀번호로 로그인 실패")
    @Test
    void login_WrongPassword_Fails() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("correctpassword");
        user.setNickname("테스트유저");
        userRepository.save(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "password": "wrongpassword"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("존재하지 않는 사용자로 로그인 실패")
    @Test
    void login_NonExistentUser_Fails() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "nonexistent",
                                "password": "password123"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("현재 사용자 정보 조회")
    @Test
    void getCurrentUser_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setNickname("테스트유저");
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "password": "password123"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        String token = loginResult.getResponse().getContentAsString()
                .replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("테스트유저"));
    }

    @DisplayName("인증 없이 사용자 정보 조회 실패")
    @Test
    void getCurrentUser_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setNickname("테스트유저");
        userRepository.save(user);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "testuser",
                                "password": "password123"
                            }
                            """))
                .andExpect(status().isOk())
                .andReturn();

        String token = loginResult.getResponse().getContentAsString()
                .replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
