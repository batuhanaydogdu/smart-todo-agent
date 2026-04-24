package com.example.agent.service;

import com.example.agent.entity.ChatMessageEntity;
import com.example.agent.entity.ChatSession;
import com.example.agent.repository.ChatMessageEntityRepository;
import com.example.agent.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SessionService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageEntityRepository messageRepository;

    public SessionService(ChatSessionRepository sessionRepository,
                          ChatMessageEntityRepository messageRepository) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
    }

    public List<ChatSession> listSessions() {
        return sessionRepository.findAllByOrderByLastActivityAtDesc();
    }

    public ChatSession createSession(String name) {
        ChatSession session = new ChatSession(UUID.randomUUID().toString(), name);
        return sessionRepository.save(session);
    }

    public List<ChatMessageEntity> getMessages(String sessionId) {
        return messageRepository.findByConversationIdOrderByCreatedAt(sessionId)
                .stream()
                .filter(m -> m.getMessageType() != ChatMessageEntity.MessageType.SYSTEM)
                .toList();
    }

    @Transactional
    public void deleteSession(String sessionId) {
        messageRepository.deleteByConversationId(sessionId);
        sessionRepository.deleteById(sessionId);
    }

    /**
     * Session varsa lastActivityAt güncelle, yoksa oto-oluştur.
     * AgentController her chat isteğinde çağırır.
     */
    public void touchSession(String sessionId) {
        sessionRepository.findById(sessionId).ifPresentOrElse(
            session -> {
                session.setLastActivityAt(LocalDateTime.now());
                sessionRepository.save(session);
            },
            () -> sessionRepository.save(new ChatSession(sessionId, null))
        );
    }
}
