package com.warmplace.repository;

import com.warmplace.entity.ChatRoomUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {
    List<ChatRoomUser> findByUserId(String userId);
    boolean existsByRoomIdAndUserId(String roomId, String userId);
}
