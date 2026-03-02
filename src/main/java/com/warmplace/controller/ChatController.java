package com.warmplace.controller;

import com.warmplace.entity.ChatRoom;
import com.warmplace.model.ChatMessage;
import com.warmplace.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "채팅 API", description = "메신저 서비스 관련 API")
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage processMessage(@Payload ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        if (message.getMessageId() == null) {
            message.setMessageId(java.util.UUID.randomUUID().toString());
        }
        return message;
    }

    @PostMapping("/api/chat/direct")
    @Operation(summary = "1:1 채팅 메시지 전송", description = "사용자 간 1:1 채팅 메시지를 전송합니다.")
    public ResponseEntity<Map<String, String>> sendDirectMessage(
            @Parameter(description = "채팅 요청 정보") @Validated @RequestBody DirectMessageRequest request) {
        
        chatService.sendDirectMessage(
            request.getSenderId(),
            request.getSenderName(),
            request.getReceiverId(),
            request.getContent()
        );
        
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/api/chat/group")
    @Operation(summary = "그룹 채팅 메시지 전송", description = "그룹 채팅방에 메시지를 전송합니다.")
    public ResponseEntity<Map<String, String>> sendGroupMessage(
            @Parameter(description = "그룹 채팅 요청 정보") @Validated @RequestBody GroupMessageRequest request) {
        
        chatService.sendGroupMessage(
            request.getSenderId(),
            request.getSenderName(),
            request.getRoomId(),
            request.getContent()
        );
        
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/api/chat/typing")
    @Operation(summary = "1:1 입력 중 알림", description = "상대방에게 입력 중임을 알립니다.")
    public ResponseEntity<Map<String, String>> sendTyping(
            @Parameter(description = "입력 중 요청 정보") @Validated @RequestBody TypingRequest request) {
        
        chatService.sendTypingIndicator(
            request.getSenderId(),
            request.getReceiverId(),
            request.isTyping()
        );
        
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/api/chat/group/typing")
    @Operation(summary = "그룹 입력 중 알림", description = "그룹 채팅방에 입력 중임을 알립니다.")
    public ResponseEntity<Map<String, String>> sendGroupTyping(
            @Parameter(description = "그룹 입력 중 요청 정보") @Validated @RequestBody GroupTypingRequest request) {
        
        chatService.sendGroupTypingIndicator(
            request.getSenderId(),
            request.getRoomId(),
            request.isTyping()
        );
        
        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/api/chat/join")
    @Operation(summary = "그룹 채팅방 입장", description = "그룹 채팅방에 참여합니다.")
    public ResponseEntity<Map<String, String>> joinRoom(
            @Parameter(description = "입장 요청 정보") @Validated @RequestBody JoinRoomRequest request) {
        
        chatService.joinRoom(
            request.getSenderId(),
            request.getSenderName(),
            request.getRoomId()
        );
        
        return ResponseEntity.ok(Map.of("status", "joined"));
    }

    @PostMapping("/api/chat/leave")
    @Operation(summary = "그룹 채팅방 퇴장", description = "그룹 채팅방에서 나갑니다.")
    public ResponseEntity<Map<String, String>> leaveRoom(
            @Parameter(description = "퇴장 요청 정보") @Validated @RequestBody JoinRoomRequest request) {
        
        chatService.leaveRoom(
            request.getSenderId(),
            request.getSenderName(),
            request.getRoomId()
        );
        
        return ResponseEntity.ok(Map.of("status", "left"));
    }

    @GetMapping("/api/chat/direct/{userId1}/{userId2}")
    @Operation(summary = "1:1 채팅 메시지 조회", description = "두 사용자 간의 채팅 메시지를 조회합니다.")
    public ResponseEntity<List<ChatMessage>> getDirectMessages(
            @Parameter(description = "첫 번째 사용자 ID") @PathVariable String userId1,
            @Parameter(description = "두 번째 사용자 ID") @PathVariable String userId2) {
        
        List<ChatMessage> messages = chatService.getDirectMessages(userId1, userId2);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/api/chat/group/{roomId}")
    @Operation(summary = "그룹 채팅 메시지 조회", description = "그룹 채팅방의 메시지를 조회합니다.")
    public ResponseEntity<List<ChatMessage>> getGroupMessages(
            @Parameter(description = "그룹 채팅방 ID") @PathVariable String roomId) {
        
        List<ChatMessage> messages = chatService.getGroupMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/api/chat/rooms/{userId}")
    @Operation(summary = "채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    public ResponseEntity<List<ChatRoomResponse>> getChatRooms(
            @Parameter(description = "사용자 ID") @PathVariable String userId) {
        
        List<ChatRoom> rooms = chatService.getChatRooms(userId);
        List<ChatRoomResponse> responses = rooms.stream()
                .map(room -> ChatRoomResponse.builder()
                        .id(room.getRoomId())
                        .name(room.getName())
                        .type(room.getType().name().toLowerCase())
                        .lastMessage(room.getLastMessage())
                        .lastMessageTime(room.getLastMessageTime() != null ? room.getLastMessageTime().toString() : null)
                        .unreadCount(room.getUnreadCount())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/chat/rooms/{roomId}/messages")
    @Operation(summary = "채팅방 메시지 조회", description = "채팅방의 메시지를 조회합니다.")
    public ResponseEntity<List<ChatMessage>> getRoomMessages(
            @Parameter(description = "채팅방 ID") @PathVariable String roomId,
            @Parameter(description = "채팅방 타입") @RequestParam(defaultValue = "direct") String type) {
        
        List<ChatMessage> messages;
        if ("group".equals(type)) {
            messages = chatService.getGroupMessages(roomId);
        } else {
            String[] parts = roomId.replace("direct_", "").split("_");
            if (parts.length >= 2) {
                messages = chatService.getDirectMessages(parts[0], parts[1]);
            } else {
                messages = List.of();
            }
        }
        return ResponseEntity.ok(messages);
    }

    @Data
    @Builder
    @Tag(name = "Response DTO", description = "응답 DTO 클래스")
    public static class ChatRoomResponse {
        private String id;
        private String name;
        private String type;
        private String lastMessage;
        private String lastMessageTime;
        private Integer unreadCount;
    }

    @Data
    @Tag(name = "Request DTO", description = "요청 DTO 클래스")
    public static class DirectMessageRequest {
        @NotBlank(message = "보내는 사용자 ID는 필수입니다")
        @Parameter(description = "보내는 사용자 ID", required = true)
        private String senderId;
        
        @NotBlank(message = "보내는 사용자 이름은 필수입니다")
        @Parameter(description = "보내는 사용자 이름", required = true)
        private String senderName;
        
        @NotBlank(message = "받는 사용자 ID는 필수입니다")
        @Parameter(description = "받는 사용자 ID", required = true)
        private String receiverId;
        
        @NotBlank(message = "메시지 내용은 필수입니다")
        @Parameter(description = "메시지 내용", required = true)
        private String content;
    }

    @Data
    public static class GroupMessageRequest {
        @NotBlank(message = "보내는 사용자 ID는 필수입니다")
        @Parameter(description = "보내는 사용자 ID", required = true)
        private String senderId;
        
        @NotBlank(message = "보내는 사용자 이름은 필수입니다")
        @Parameter(description = "보내는 사용자 이름", required = true)
        private String senderName;
        
        @NotBlank(message = "그룹 채팅방 ID는 필수입니다")
        @Parameter(description = "그룹 채팅방 ID", required = true)
        private String roomId;
        
        @NotBlank(message = "메시지 내용은 필수입니다")
        @Parameter(description = "메시지 내용", required = true)
        private String content;
    }

    @Data
    public static class TypingRequest {
        @NotBlank(message = "보내는 사용자 ID는 필수입니다")
        @Parameter(description = "보내는 사용자 ID", required = true)
        private String senderId;
        
        @NotBlank(message = "받는 사용자 ID는 필수입니다")
        @Parameter(description = "받는 사용자 ID", required = true)
        private String receiverId;
        
        @Parameter(description = "입력 중 여부", required = true)
        private boolean isTyping;
    }

    @Data
    public static class GroupTypingRequest {
        @NotBlank(message = "보내는 사용자 ID는 필수입니다")
        @Parameter(description = "보내는 사용자 ID", required = true)
        private String senderId;
        
        @NotBlank(message = "그룹 채팅방 ID는 필수입니다")
        @Parameter(description = "그룹 채팅방 ID", required = true)
        private String roomId;
        
        @Parameter(description = "입력 중 여부", required = true)
        private boolean isTyping;
    }

    @Data
    public static class JoinRoomRequest {
        @NotBlank(message = "보내는 사용자 ID는 필수입니다")
        @Parameter(description = "보내는 사용자 ID", required = true)
        private String senderId;
        
        @NotBlank(message = "보내는 사용자 이름은 필수입니다")
        @Parameter(description = "보내는 사용자 이름", required = true)
        private String senderName;
        
        @NotBlank(message = "그룹 채팅방 ID는 필수입니다")
        @Parameter(description = "그룹 채팅방 ID", required = true)
        private String roomId;
    }
}
