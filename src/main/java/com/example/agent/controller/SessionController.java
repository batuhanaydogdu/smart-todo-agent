package com.example.agent.controller;

import com.example.agent.entity.ChatMessageEntity;
import com.example.agent.entity.ChatSession;
import com.example.agent.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Oturum yönetimi endpoint'leri.
 *
 * GET    /sessions                → tüm oturumları listele
 * POST   /sessions                → yeni oturum oluştur
 * GET    /sessions/{id}/messages  → oturumun mesaj geçmişi
 * DELETE /sessions/{id}           → oturumu sil
 */
@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public List<SessionDto> listSessions() {
        return sessionService.listSessions().stream()
                .map(s -> new SessionDto(s.getId(), s.getName(), s.getLastActivityAt()))
                .toList();
    }

    @PostMapping
    public SessionDto createSession(@RequestBody CreateSessionRequest request) {
        ChatSession session = sessionService.createSession(request.name());
        return new SessionDto(session.getId(), session.getName(), session.getLastActivityAt());
    }

    @GetMapping("/{id}/messages")
    public List<MessageDto> getMessages(@PathVariable String id) {
        return sessionService.getMessages(id).stream()
                .map(m -> new MessageDto(m.getMessageType().name(), m.getContent()))
                .toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    record SessionDto(String id, String name, LocalDateTime lastActivityAt) {}
    record MessageDto(String role, String content) {}
    record CreateSessionRequest(String name) {}
}
