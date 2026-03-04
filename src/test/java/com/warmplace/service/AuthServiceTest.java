package com.warmplace.service;

import com.warmplace.dto.*;
import com.warmplace.entity.User;
import com.warmplace.exception.DuplicateEmailException;
import com.warmplace.exception.DuplicateUsernameException;
import com.warmplace.repository.UserRepository;
import com.warmplace.security.JwtTokenProvider;
import com.warmplace.security.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuthService authService;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setNickname("테스트유저");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트유저")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @DisplayName("회원가입 성공")
    @Test
    void signup_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        AuthResponse response = authService.signup(signupRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
        verify(redisService).saveToken(eq("testuser"), eq("jwt-token"), anyLong());
    }

    @DisplayName("중복 username으로 회원가입 실패")
    @Test
    void signup_DuplicateUsername_Fails() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(DuplicateUsernameException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("중복 email로 회원가입 실패")
    @Test
    void signup_DuplicateEmail_Fails() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @DisplayName("로그인 성공")
    @Test
    void login_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("testuser")).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);
        when(redisService.hasToken("testuser")).thenReturn(false);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        verify(redisService).saveToken(eq("testuser"), eq("jwt-token"), anyLong());
    }

    @DisplayName("잘못된 비밀번호로 로그인 실패")
    @Test
    void login_WrongPassword_Fails() {
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        lenient().when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .hasMessageContaining("사용자 이름 또는 비밀번호");
    }

    @DisplayName("존재하지 않는 사용자로 로그인 실패")
    @Test
    void login_NonExistentUser_Fails() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        assertThatThrownBy(() -> authService.login(request))
                .hasMessageContaining("사용자 이름");
    }

    @DisplayName("비활성화된 계정으로 로그인 실패")
    @Test
    void login_InactiveUser_Fails() {
        User inactiveUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스트유저")
                .isActive(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));
        lenient().when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .hasMessageContaining("비활성화");
    }

    @DisplayName("로그아웃 성공")
    @Test
    void logout_Success() {
        authService.logout("testuser");

        verify(redisService).deleteToken("testuser");
    }

    @DisplayName("현재 사용자 조회")
    @Test
    void getCurrentUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserResponse response = authService.getCurrentUser("testuser");

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @DisplayName("사용자 존재 여부 확인")
    @Test
    void checkUserExists_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        boolean exists = authService.checkUserExists("testuser");

        assertThat(exists).isTrue();
    }

    @DisplayName("모든 사용자 조회")
    @Test
    void getAllUsers_Success() {
        User inactiveUser = User.builder()
                .id(2L)
                .username("inactive")
                .email("inactive@example.com")
                .password("password")
                .nickname("비활성")
                .isActive(false)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(testUser, inactiveUser));

        List<UserResponse> users = authService.getAllUsers();

        assertThat(users).hasSize(1);
        assertThat(users.get(0).getUsername()).isEqualTo("testuser");
    }
}
