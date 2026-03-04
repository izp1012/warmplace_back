package com.warmplace.service;

import com.warmplace.entity.ChatRoom;
import com.warmplace.entity.ChatRoomUser;
import com.warmplace.model.ChatMessage;
import com.warmplace.producer.ChatProducer;
import com.warmplace.document.ChatMessageDocument;
import com.warmplace.repository.ChatMessageMongoRepository;
import com.warmplace.repository.ChatRoomRepository;
import com.warmplace.repository.ChatRoomUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatProducer chatProducer;

    @Mock
    private ChatMessageMongoRepository chatMessageMongoRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomUserRepository chatRoomUserRepository;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
    }

    @DisplayName("1:1 메시지 전송 성공")
    @Test
    void sendDirectMessage_Success() {
        when(chatRoomRepository.findByRoomId(anyString())).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatRoomUserRepository.save(any(ChatRoomUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatService.sendDirectMessage("sender1", "보내는이", "receiver1", "메시지 내용");

        verify(chatProducer).sendToUser(eq("receiver1"), any(ChatMessage.class));
        verify(chatRoomRepository, times(2)).save(any(ChatRoom.class));
    }

    @DisplayName("1:1 메시지 전송 - senderId 누락 시 예외")
    @Test
    void sendDirectMessage_NullSenderId_Fails() {
        assertThatThrownBy(() -> chatService.sendDirectMessage(null, "name", "receiver", "content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보내는 사용자 ID");
    }

    @DisplayName("1:1 메시지 전송 - receiverId 누락 시 예외")
    @Test
    void sendDirectMessage_NullReceiverId_Fails() {
        assertThatThrownBy(() -> chatService.sendDirectMessage("sender", "name", null, "content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("받는 사용자 ID");
    }

    @DisplayName("1:1 메시지 전송 - 내용 누락 시 예외")
    @Test
    void sendDirectMessage_NullContent_Fails() {
        assertThatThrownBy(() -> chatService.sendDirectMessage("sender", "name", "receiver", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지 내용");
    }

    @DisplayName("그룹 메시지 전송 성공")
    @Test
    void sendGroupMessage_Success() {
        when(chatRoomRepository.findByRoomId(anyString())).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatRoomUserRepository.existsByRoomIdAndUserId(anyString(), anyString())).thenReturn(false);
        when(chatRoomUserRepository.save(any(ChatRoomUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatService.sendGroupMessage("sender1", "보내는이", "room1", "그룹 메시지");

        verify(chatProducer).sendToGroup(eq("room1"), any(ChatMessage.class));
    }

    @DisplayName("그룹 메시지 전송 - roomId 누락 시 예외")
    @Test
    void sendGroupMessage_NullRoomId_Fails() {
        assertThatThrownBy(() -> chatService.sendGroupMessage("sender", "name", null, "content"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("그룹 채팅방 ID");
    }

    @DisplayName("그룹 메시지 전송 - 내용 누락 시 예외")
    @Test
    void sendGroupMessage_NullContent_Fails() {
        assertThatThrownBy(() -> chatService.sendGroupMessage("sender", "name", "room", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지 내용");
    }

    @DisplayName("1:1 입력 중 알림 전송")
    @Test
    void sendTypingIndicator_Success() {
        chatService.sendTypingIndicator("sender", "receiver", true);

        verify(chatProducer).sendToUser(eq("receiver"), any(ChatMessage.class));
    }

    @DisplayName("그룹 입력 중 알림 전송")
    @Test
    void sendGroupTypingIndicator_Success() {
        chatService.sendGroupTypingIndicator("sender", "room1", true);

        verify(chatProducer).sendToGroup(eq("room1"), any(ChatMessage.class));
    }

    @DisplayName("그룹 채팅방 입장")
    @Test
    void joinRoom_Success() {
        when(chatRoomRepository.findByRoomId(anyString())).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatRoomUserRepository.existsByRoomIdAndUserId(anyString(), anyString())).thenReturn(false);
        when(chatRoomUserRepository.save(any(ChatRoomUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatService.joinRoom("user1", "사용자1", "room1");

        verify(chatProducer).sendToGroup(eq("room1"), any(ChatMessage.class));
    }

    @DisplayName("그룹 채팅방 퇴장")
    @Test
    void leaveRoom_Success() {
        chatService.leaveRoom("user1", "사용자1", "room1");

        verify(chatProducer).sendToGroup(eq("room1"), any(ChatMessage.class));
    }

    @DisplayName("1:1 채팅 메시지 조회")
    @Test
    void getDirectMessages_Success() {
        ChatMessageDocument doc = ChatMessageDocument.builder()
                .messageId("msg1")
                .senderId("sender1")
                .receiverId("receiver1")
                .roomId("direct_sender1_receiver1")
                .content("메시지")
                .type(ChatMessageDocument.MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        when(chatRoomRepository.findByRoomId(anyString())).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatRoomUserRepository.save(any(ChatRoomUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageMongoRepository.findByRoomIdOrderByTimestampAsc(anyString())).thenReturn(List.of(doc));

        List<ChatMessage> messages = chatService.getDirectMessages("sender1", "receiver1");

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("메시지");
    }

    @DisplayName("그룹 채팅 메시지 조회")
    @Test
    void getGroupMessages_Success() {
        ChatMessageDocument doc = ChatMessageDocument.builder()
                .messageId("msg1")
                .senderId("sender1")
                .roomId("room1")
                .content("그룹 메시지")
                .type(ChatMessageDocument.MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        when(chatMessageMongoRepository.findByRoomIdOrderByTimestampAsc("room1")).thenReturn(List.of(doc));

        List<ChatMessage> messages = chatService.getGroupMessages("room1");

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getContent()).isEqualTo("그룹 메시지");
    }

    @DisplayName("채팅방 목록 조회")
    @Test
    void getChatRooms_Success() {
        ChatRoomUser cru = ChatRoomUser.builder()
                .roomId("room1")
                .userId("user1")
                .userName("사용자1")
                .build();

        ChatRoom room = ChatRoom.builder()
                .roomId("room1")
                .name("채팅방1")
                .type(ChatRoom.RoomType.DIRECT)
                .lastMessageTime(LocalDateTime.now())
                .build();

        when(chatRoomUserRepository.findByUserId("user1")).thenReturn(List.of(cru));
        when(chatRoomRepository.findByRoomId("room1")).thenReturn(Optional.of(room));

        List<ChatRoom> rooms = chatService.getChatRooms("user1");

        assertThat(rooms).hasSize(1);
        assertThat(rooms.get(0).getRoomId()).isEqualTo("room1");
    }
}
