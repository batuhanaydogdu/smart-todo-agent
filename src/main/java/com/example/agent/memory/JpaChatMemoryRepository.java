package com.example.agent.memory;

import com.example.agent.entity.ChatMessageEntity;
import com.example.agent.entity.ChatMessageEntity.MessageType;
import com.example.agent.repository.ChatMessageEntityRepository;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ChatMemoryRepository'nin JPA implementasyonu.
 *
 * Spring AI, MessageWindowChatMemory üzerinden bu interface'i kullanır:
 *   findByConversationId() → mesajları DB'den yükle
 *   saveAll()              → tüm listeyi DB'ye yaz (delete + insert)
 *   deleteByConversationId() → oturumu temizle
 *
 * InMemoryChatMemoryRepository'nin tek farkı:
 *   veriler RAM yerine H2 dosyasına kaydedilir → restart'ta kaybolmaz.
 */
@Component
public class JpaChatMemoryRepository implements ChatMemoryRepository {

    private final ChatMessageEntityRepository messageRepository;

    public JpaChatMemoryRepository(ChatMessageEntityRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        return messageRepository
                .findByConversationIdOrderByCreatedAt(conversationId)
                .stream()
                .map(this::toMessage)
                .toList();
    }

    /**
     * MessageWindowChatMemory her mesajdan sonra tüm listeyi saveAll ile yazar.
     * Bu yüzden önce eski kayıtları silip yenilerini ekliyoruz.
     */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        messageRepository.deleteByConversationId(conversationId);
        List<ChatMessageEntity> entities = messages.stream()
                .map(m -> toEntity(conversationId, m))
                .toList();
        messageRepository.saveAll(entities);
    }

    @Override
    public List<String> findConversationIds() {
        return messageRepository.findDistinctConversationIds();
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        messageRepository.deleteByConversationId(conversationId);
    }

    // Spring AI Message → DB entity
    private ChatMessageEntity toEntity(String conversationId, Message message) {
        MessageType type;
        if (message instanceof UserMessage) {
            type = MessageType.USER;
        } else if (message instanceof AssistantMessage) {
            type = MessageType.ASSISTANT;
        } else {
            type = MessageType.SYSTEM;
        }
        return new ChatMessageEntity(conversationId, type, message.getText());
    }

    // DB entity → Spring AI Message
    private Message toMessage(ChatMessageEntity entity) {
        return switch (entity.getMessageType()) {
            case USER      -> new UserMessage(entity.getContent());
            case ASSISTANT -> new AssistantMessage(entity.getContent());
            case SYSTEM    -> new SystemMessage(entity.getContent());
        };
    }
}
