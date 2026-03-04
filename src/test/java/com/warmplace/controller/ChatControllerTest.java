package com.warmplace.controller;

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

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .nickname("테스트유저")
                .isActive(true)
                .build();
        userRepository.save(user);

        token = getToken();
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

    @DisplayName("1:1 메시지 전송 - 내용 누락 시 실패")
    @Test
    void sendDirectMessage_EmptyContent_Fails() throws Exception {
        mockMvc.perform(post("/api/chat/direct")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "receiverId": "receiver123",
                                "content": ""
                            }
                            """))
                .andExpect(status().isBadRequest());
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

    @DisplayName("그룹 메시지 전송 - roomId 누락 시 실패")
    @Test
    void sendGroupMessage_EmptyRoomId_Fails() throws Exception {
        mockMvc.perform(post("/api/chat/group")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "senderId": "testuser",
                                "senderName": "테스트유저",
                                "roomId": "",
                                "content": "그룹 메시지입니다!"
                            }
                            """))
                .andExpect(status().isBadRequest());
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

    @DisplayName("1:1 채팅 메시지 조회")
    @Test
    void getDirectMessages_Success() throws Exception {
        mockMvc.perform(get("/api/chat/direct/testuser/receiver123")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @DisplayName("그룹 채팅 메시지 조회")
    @Test
    void getGroupMessages_Success() throws Exception {
        mockMvc.perform(get("/api/chat/group/general")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @DisplayName("채팅방 목록 조회")
    @Test
    void getChatRooms_Success() throws Exception {
        mockMvc.perform(get("/api/chat/rooms/testuser")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private String getToken() throws Exception {
        String loginJson = """
            {
                "username": "testuser",
                "password": "password123"
            }
            """;
        
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
    }
}
