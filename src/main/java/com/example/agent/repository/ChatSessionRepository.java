package com.example.agent.repository;

import com.example.agent.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    // Son aktiviteye göre azalan sırada — en son kullanılan üstte
    List<ChatSession> findAllByOrderByLastActivityAtDesc();
}
