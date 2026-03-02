package com.warmplace;

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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setNickname("테스트유저");
        userRepository.save(user);

        String loginJson = """
            {
                "username": "testuser",
                "password": "password123"
            }
            """;

        token = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @DisplayName("1:1 메시지 전송 성공")
    @Test
    void sendDirectMessage_Success() throws Exception {
        mockMvc.perform(post("/api/chat/direct")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "receiverId": "receiver123",
                                "content": "안녕하세요!"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"));
    }

    @DisplayName("그룹 메시지 전송 성공")
    @Test
    void sendGroupMessage_Success() throws Exception {
        mockMvc.perform(post("/api/chat/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "roomId": "general",
                                "content": "그룹 메시지입니다!"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"));
    }

    @DisplayName("1:1 입력 중 알림 전송")
    @Test
    void sendTyping_Success() throws Exception {
        mockMvc.perform(post("/api/chat/typing")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "receiverId": "receiver123",
                                "isTyping": true
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"));
    }

    @DisplayName("그룹 입력 중 알림 전송")
    @Test
    void sendGroupTyping_Success() throws Exception {
        mockMvc.perform(post("/api/chat/group/typing")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "roomId": "general",
                                "isTyping": true
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("sent"));
    }

    @DisplayName("그룹 채팅방 입장")
    @Test
    void joinRoom_Success() throws Exception {
        mockMvc.perform(post("/api/chat/join")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "roomId": "general"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("joined"));
    }

    @DisplayName("그룹 채팅방 퇴장")
    @Test
    void leaveRoom_Success() throws Exception {
        mockMvc.perform(post("/api/chat/leave")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "roomId": "general"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("left"));
    }

    @DisplayName("인증 없이 메시지 전송 실패")
    @Test
    void sendMessage_WithoutAuth_Fails() throws Exception {
        mockMvc.perform(post("/api/chat/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "receiverId": "receiver123",
                                "content": "안녕하세요!"
                            }
                            """))
                .andExpect(status().isUnauthorized());
    }
}
