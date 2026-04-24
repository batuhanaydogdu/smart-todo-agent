package com.example.agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Konuşma mesajlarını DB'ye kaydetmek için JPA entity.
 *
 * Spring AI'ın Message interface'i doğrudan persist edilemez (interface).
 * Bu yüzden mesajları düz tablo satırlarına dönüştürüp saklıyoruz:
 *   conversationId → hangi oturuma ait
 *   messageType    → USER / ASSISTANT / SYSTEM
 *   content        → mesaj metni
 */
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_conversation_id", columnList = "conversationId")
})
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String conversationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime createdAt;

    public enum MessageType {
        USER, ASSISTANT, SYSTEM
    }

    public ChatMessageEntity() {}

    public ChatMessageEntity(String conversationId, MessageType messageType, String content) {
        this.conversationId = conversationId;
        this.messageType = messageType;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public MessageType getMessageType() { return messageType; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
