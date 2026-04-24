package com.example.agent.service;

import com.example.agent.tools.TodoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import com.example.agent.memory.JpaChatMemoryRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * AI Agent servisi.
 *
 * Nasıl çalışır:
 * 1. Kullanıcı mesajı + sessionId gelir
 * 2. MessageChatMemoryAdvisor o session'ın geçmiş mesajlarını prompt'a ekler
 * 3. ChatClient mesajı + geçmiş → Ollama/Gemma4
 * 4. LLM tool çağırır veya direkt cevap verir
 * 5. Yeni mesaj çifti (user + assistant) geçmişe kaydedilir
 *
 * sessionId: her oturumu birbirinden ayırır.
 * Aynı sessionId → LLM önceki konuşmayı hatırlar.
 * Farklı sessionId → sıfırdan başlar.
 */
@Service
public class AgentService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public AgentService(OllamaChatModel ollamaChatModel, TodoTools todoTools,
                        JpaChatMemoryRepository jpaChatMemoryRepository) {
        // JpaChatMemoryRepository: mesajlar H2 dosyasına kaydedilir → restart'ta kaybolmaz.
        // MessageWindowChatMemory: son N mesajı tutar (varsayılan 20).
        this.chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jpaChatMemoryRepository)
                .build();

        this.chatClient = ChatClient.builder(ollamaChatModel)
            .defaultSystem("""
                Sen akıllı bir görev yönetimi asistanısın.
                Kullanıcının doğal dil isteklerini anlayıp uygun araçları kullanarak TODO görevlerini yönet.
                Türkçe konuş. Kısa ve net cevaplar ver.
                Bir görevi yaparken kaç tane tool çağırdığını açıkla — bu öğretici olacak.

                ÖNEMLİ: Kullanıcı proje, sprint, görev listesi veya spesifik bilgi içeren bir şey hakkında soru sorarsa
                MUTLAKA önce searchDocuments tool'unu kullan. Kendi bilginle cevap verme,
                dokümanda ne yazıyorsa onu kullan.
                """)
            .defaultTools(todoTools)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
            .build();
    }

    public String chat(String userMessage, String sessionId) {
        return chatClient.prompt()
            .user(userMessage)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
            .call()
            .content();
    }

    /**
     * Streaming versiyon: cevabı token token döner.
     * Controller'da text/event-stream olarak sunulur.
     * UI her token geldiğinde ekrana yazar → ChatGPT benzeri deneyim.
     */
    public Flux<String> chatStream(String userMessage, String sessionId) {
        return chatClient.prompt()
            .user(userMessage)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
            .stream()
            .content();
    }
}
