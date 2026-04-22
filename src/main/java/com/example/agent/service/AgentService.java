package com.example.agent.service;

import com.example.agent.tools.TodoTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

/**
 * AI Agent servisi.
 *
 * Nasıl çalışır:
 * 1. Kullanıcı mesajı gelir (örn: "Yüksek öncelikli bir bug fix görevi ekle")
 * 2. ChatClient mesajı Ollama/Gemma4'e gönderir
 * 3. LLM mesajı analiz eder ve hangi tool'u çağıracağına karar verir
 * 4. Spring AI otomatik olarak ilgili TodoTools method'unu çalıştırır
 * 5. Tool sonucu LLM'e geri döner, LLM kullanıcıya cevap üretir
 * 6. Karmaşık isteklerde bu döngü birden fazla kez tekrarlanabilir (multi-step)
 *
 * Model değiştirmek için: OllamaChatModel yerine AnthropicChatModel inject et
 * ve application.yml'de ilgili bloğu aç.
 */
@Service
public class AgentService {

    private final ChatClient chatClient;

    // OllamaChatModel açıkça inject ediyoruz — hem Ollama hem Anthropic starter
    // aynı anda classpath'te olduğunda Spring hangisini kullanacağını bilemez.
    public AgentService(OllamaChatModel ollamaChatModel, TodoTools todoTools) {
        this.chatClient = ChatClient.builder(ollamaChatModel)
            .defaultSystem("""
                Sen akıllı bir görev yönetimi asistanısın.
                Kullanıcının doğal dil isteklerini anlayıp uygun araçları kullanarak TODO görevlerini yönet.
                Türkçe konuş. Kısa ve net cevaplar ver.
                Bir görevi yaparken kaç tane tool çağırdığını açıkla — bu öğretici olacak.
                """)
            .defaultTools(todoTools)
            .build();
    }

    public String chat(String userMessage) {
        return chatClient.prompt()
            .user(userMessage)
            .call()
            .content();
    }
}
