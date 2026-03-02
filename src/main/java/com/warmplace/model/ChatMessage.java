package com.warmplace.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private String roomId;
    
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        READ
    }
}
