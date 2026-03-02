package com.warmplace.service;

import com.warmplace.dto.*;
import com.warmplace.entity.User;
import com.warmplace.exception.DuplicateEmailException;
import com.warmplace.exception.DuplicateUsernameException;
import com.warmplace.repository.UserRepository;
import com.warmplace.security.JwtTokenProvider;
import com.warmplace.security.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUsernameException();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .isActive(true)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getUsername());
        redisService.saveToken(user.getUsername(), token, jwtTokenProvider.getExpirationTime());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자 이름 또는 비밀번호가 올바르지 않습니다"));

        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된 계정입니다");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("사용자 이름 또는 비밀번호가 올바르지 않습니다");
        }

        if (redisService.hasToken(user.getUsername())) {
            redisService.deleteToken(user.getUsername());
        }

        String token = jwtTokenProvider.generateToken(user.getUsername());
        redisService.saveToken(user.getUsername(), token, jwtTokenProvider.getExpirationTime());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime())
                .user(mapToUserResponse(user))
                .build();
    }

    @Transactional
    public void logout(String username) {
        redisService.deleteToken(username);
    }

    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        return mapToUserResponse(user);
    }

    public boolean checkUserExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(User::getIsActive)
                .map(this::mapToUserResponse)
                .toList();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImage(user.getProfileImage())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
