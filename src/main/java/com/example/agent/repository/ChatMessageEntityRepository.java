package com.example.agent.repository;

import com.example.agent.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface ChatMessageEntityRepository extends JpaRepository<ChatMessageEntity, Long> {

    // Bir oturumun tüm mesajlarını oluşturulma sırasına göre getir
    List<ChatMessageEntity> findByConversationIdOrderByCreatedAt(String conversationId);

    // Tüm benzersiz conversationId'leri getir (ChatMemoryRepository.findConversationIds için)
    @Query("SELECT DISTINCT m.conversationId FROM ChatMessageEntity m")
    List<String> findDistinctConversationIds();

    // saveAll çağrıldığında tüm listeyi sil, yeniden yaz
    @Modifying
    @Transactional
    void deleteByConversationId(String conversationId);
}
