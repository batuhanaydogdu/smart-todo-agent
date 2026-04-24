package com.example.agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Kullanıcı oturumlarını temsil eder.
 * Her oturumun bir adı (kullanıcı tarafından verilir) ve UUID'si vardır.
 */
@Entity
@Table(name = "chat_sessions")
public class ChatSession {

    @Id
    private String id; // UUID — kullanıcı tarafından üretilir

    @Column(nullable = false)
    private String name; // kullanıcının verdiği isim: "İş", "Kişisel" vb.

    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;

    public ChatSession() {}

    public ChatSession(String id, String name) {
        this.id = id;
        LocalDateTime now = LocalDateTime.now();
        // İsim verilmemişse "Oturum 14:32" formatında üret — count'a bağımlı değil
        this.name = (name != null && !name.isBlank())
                ? name
                : "Oturum " + now.format(DateTimeFormatter.ofPattern("HH:mm"));
        this.createdAt = now;
        this.lastActivityAt = now;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }
}
