package com.warmplace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_room_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String roomId;

    @Column(nullable = false, length = 50)
    private String userId;

    @Column(nullable = false, length = 100)
    private String userName;
}
