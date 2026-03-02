package com.warmplace.service;

import com.warmplace.entity.ChatRoom;
import com.warmplace.entity.ChatRoomUser;
import com.warmplace.model.ChatMessage;
import com.warmplace.producer.ChatProducer;
import com.warmplace.document.ChatMessageDocument;
import com.warmplace.repository.ChatMessageMongoRepository;
import com.warmplace.repository.ChatRoomRepository;
import com.warmplace.repository.ChatRoomUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatProducer chatProducer;
    private final ChatMessageMongoRepository chatMessageMongoRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;

    public void sendDirectMessage(String senderId, String senderName, String receiverId, String content) {
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("보내는 사용자 ID는 필수입니다");
        }
        if (receiverId == null || receiverId.isBlank()) {
            throw new IllegalArgumentException("받는 사용자 ID는 필수입니다");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다");
        }

        String roomId = generateDirectRoomId(senderId, receiverId);

        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .senderName(senderName != null ? senderName : senderId)
                .receiverId(receiverId)
                .roomId(roomId)
                .content(content)
                .type(ChatMessage.MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        chatProducer.sendToUser(receiverId, message);
        updateOrCreateChatRoom(senderId, receiverId, senderName, receiverId, content);
        log.info("Direct message sent from {} to {}", senderId, receiverId);
    }

    @Transactional
    public void saveDirectMessage(ChatMessage message) {
        ChatMessageDocument document = ChatMessageDocument.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .receiverId(message.getReceiverId())
                .roomId(message.getRoomId())
                .content(message.getContent())
                .type(ChatMessageDocument.MessageType.valueOf(message.getType().name()))
                .timestamp(message.getTimestamp())
                .build();
        chatMessageMongoRepository.save(document);
    }

    @Transactional
    public void updateOrCreateChatRoom(String userId1, String userId2, String userName1, String userName2, String lastMessage) {
        String roomId = generateDirectRoomId(userId1, userId2);
        
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElse(null);
        if (chatRoom == null) {
            chatRoom = ChatRoom.builder()
                    .roomId(roomId)
                    .name(userId1.compareTo(userId2) < 0 ? userId1 + "_" + userId2 : userId2 + "_" + userId1)
                    .type(ChatRoom.RoomType.DIRECT)
                    .build();
            chatRoom = chatRoomRepository.save(chatRoom);
            
            chatRoomUserRepository.save(ChatRoomUser.builder()
                    .roomId(roomId)
                    .userId(userId1)
                    .userName(userName1)
                    .build());
            chatRoomUserRepository.save(ChatRoomUser.builder()
                    .roomId(roomId)
                    .userId(userId2)
                    .userName(userName2)
                    .build());
        }
        
        chatRoom.setLastMessage(lastMessage);
        chatRoom.setLastMessageTime(LocalDateTime.now());
        chatRoomRepository.save(chatRoom);
    }

    private String generateDirectRoomId(String userId1, String userId2) {
        if (userId1.compareTo(userId2) < 0) {
            return "direct_" + userId1 + "_" + userId2;
        }
        return "direct_" + userId2 + "_" + userId1;
    }

    public void sendGroupMessage(String senderId, String senderName, String roomId, String content) {
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("보내는 사용자 ID는 필수입니다");
        }
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("그룹 채팅방 ID는 필수입니다");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 필수입니다");
        }

        ChatMessage message = ChatMessage.builder()
                .messageId(UUID.randomUUID().toString())
                .senderId(senderId)
                .senderName(senderName != null ? senderName : senderId)
                .roomId(roomId)
                .content(content)
                .type(ChatMessage.MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        chatProducer.sendToGroup(roomId, message);
        updateGroupChatRoom(roomId, content);
        log.info("Group message sent to room {} from {}", roomId, senderId);
    }

    @Transactional
    public void saveGroupMessage(ChatMessage message) {
        ChatMessageDocument document = ChatMessageDocument.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .roomId(message.getRoomId())
                .content(message.getContent())
                .type(ChatMessageDocument.MessageType.valueOf(message.getType().name()))
                .timestamp(message.getTimestamp())
                .build();
        chatMessageMongoRepository.save(document);
    }

    @Transactional
    public void updateGroupChatRoom(String roomId, String lastMessage) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomId(roomId).orElse(null);
        if (chatRoom != null) {
            chatRoom.setLastMessage(lastMessage);
            chatRoom.setLastMessageTime(LocalDateTime.now());
            chatRoomRepository.save(chatRoom);
        }
    }

    public void sendTypingIndicator(String senderId, String receiverId, boolean isTyping) {
        if (senderId == null || senderId.isBlank()) {
            return;
        }
        if (receiverId == null || receiverId.isBlank()) {
            return;
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .type(ChatMessage.MessageType.TYPING)
                .build();
        
        chatProducer.sendToUser(receiverId, message);
    }

    public void sendGroupTypingIndicator(String senderId, String roomId, boolean isTyping) {
        if (senderId == null || senderId.isBlank()) {
            return;
        }
        if (roomId == null || roomId.isBlank()) {
            return;
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .roomId(roomId)
                .type(ChatMessage.MessageType.TYPING)
                .build();
        
        chatProducer.sendToGroup(roomId, message);
    }

    public void joinRoom(String senderId, String senderName, String roomId) {
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("보내는 사용자 ID는 필수입니다");
        }
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("그룹 채팅방 ID는 필수입니다");
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .senderName(senderName != null ? senderName : senderId)
                .roomId(roomId)
                .type(ChatMessage.MessageType.JOIN)
                .timestamp(LocalDateTime.now())
                .build();

        chatProducer.sendToGroup(roomId, message);
    }

    public void leaveRoom(String senderId, String senderName, String roomId) {
        if (senderId == null || senderId.isBlank()) {
            throw new IllegalArgumentException("보내는 사용자 ID는 필수입니다");
        }
        if (roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("그룹 채팅방 ID는 필수입니다");
        }

        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .senderName(senderName != null ? senderName : senderId)
                .roomId(roomId)
                .type(ChatMessage.MessageType.LEAVE)
                .timestamp(LocalDateTime.now())
                .build();

        chatProducer.sendToGroup(roomId, message);
    }

    public List<ChatMessage> getDirectMessages(String userId1, String userId2) {
        String roomId = generateDirectRoomId(userId1, userId2);
        List<ChatMessageDocument> documents = chatMessageMongoRepository.findByRoomIdOrderByTimestampAsc(roomId);
        return documents.stream()
                .map(this::convertDocumentToModel)
                .collect(Collectors.toList());
    }

    public List<ChatMessage> getGroupMessages(String roomId) {
        List<ChatMessageDocument> documents = chatMessageMongoRepository.findByRoomIdOrderByTimestampAsc(roomId);
        return documents.stream()
                .map(this::convertDocumentToModel)
                .collect(Collectors.toList());
    }

    public List<ChatRoom> getChatRooms(String userId) {
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findByUserId(userId);
        return chatRoomUsers.stream()
                .map(cru -> chatRoomRepository.findByRoomId(cru.getRoomId()).orElse(null))
                .filter(room -> room != null)
                .sorted((r1, r2) -> {
                    if (r1.getLastMessageTime() == null && r2.getLastMessageTime() == null) return 0;
                    if (r1.getLastMessageTime() == null) return 1;
                    if (r2.getLastMessageTime() == null) return -1;
                    return r2.getLastMessageTime().compareTo(r1.getLastMessageTime());
                })
                .collect(Collectors.toList());
    }

    private ChatMessage convertDocumentToModel(ChatMessageDocument document) {
        return ChatMessage.builder()
                .messageId(document.getMessageId())
                .senderId(document.getSenderId())
                .senderName(document.getSenderName())
                .receiverId(document.getReceiverId())
                .roomId(document.getRoomId())
                .content(document.getContent())
                .type(document.getType() != null ? 
                    ChatMessage.MessageType.valueOf(document.getType().name()) : 
                    ChatMessage.MessageType.CHAT)
                .timestamp(document.getTimestamp())
                .build();
    }
}
