# Smart TODO Agent — POC Projesi

AI Agent + MCP Server konseptlerini öğrenmek için Spring Boot + Spring AI ile yazılmış POC.

## Proje Amacı

Bu proje şu kavramları uygulamalı olarak öğretmek için tasarlanmıştır:
- **Spring AI Tool Calling**: `@Tool` annotation ile LLM'e Java method'larını araç olarak sunma
- **AI Agent döngüsü**: LLM'in otomatik tool seçimi ve multi-step reasoning
- **RAG**: Doküman yükleme, chunking, embedding ve semantik arama
- **Agent Memory**: Session bazlı konuşma geçmişi (`MessageWindowChatMemory`)
- **Streaming**: Token token cevap akışı (`Flux<String>` + SSE)
- **MCP Server**: Araçları dışarıya açma (Claude Desktop/Code bağlanabilir)
- **Model değişimi**: Claude ↔ Ollama/Gemma4 swap sadece `application.yml` ile

## Proje Yapısı

```
src/main/java/com/example/agent/
├── SmartTodoAgentApplication.java   — Spring Boot entry point
├── controller/
│   ├── AgentController.java         — POST /chat ve POST /chat/stream endpoint'leri
│   └── DocumentController.java      — POST /documents/upload (RAG dosya yükleme)
├── service/
│   ├── AgentService.java            — ChatClient, streaming ve memory konfigürasyonu
│   └── DocumentService.java         — RAG: chunking, embedding, vektör arama
├── tools/
│   └── TodoTools.java               — 7 adet @Tool annotated method (agent araçları)
├── entity/
│   └── Todo.java                    — JPA entity: title, priority, status, dueDate
└── repository/
    └── TodoRepository.java          — Spring Data JPA

src/main/resources/
├── application.yml                  — model konfigürasyonu
└── static/
    └── index.html                   — Chat UI (markdown render, streaming, RAG panel)

test-documents/
└── proje-dokumani.txt               — RAG testi için örnek döküman
```

## Çalıştırma

### Gereksinimler
- Java 21+
- Maven 3.9+
- Anthropic API key **veya** Ollama kurulu (lokal Gemma4)

### Claude ile çalıştırma
```bash
export ANTHROPIC_API_KEY=sk-ant-...
mvn spring-boot:run
```

### Ollama/Gemma4 ile çalıştırma
`application.yml` içinde Anthropic bloğunu comment out et, Ollama bloğunu aç:
```yaml
spring.ai.ollama.base-url: http://localhost:11434
spring.ai.ollama.chat.options.model: gemma4
```
Sonra:
```bash
mvn spring-boot:run
```
> **Not:** Ollama + Gemma4 tool call streaming'inde bug olabilir. Sorun yaşarsan
> `spring.ai.ollama.chat.options.stream: false` ekle.

## Kullanım

### Web UI
```
http://localhost:8080
```
Markdown render, streaming, doküman yükleme paneli dahil.

### REST API — Chat Endpoint (senkron)
```bash
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Login bug fix taskını yüksek öncelikli ekle", "sessionId": "oturum-1"}'
```

### REST API — Streaming Endpoint (token token)
```bash
curl -X POST http://localhost:8080/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message": "Görevlerimi listele", "sessionId": "oturum-1"}'
# Cevap: data:İşte data: görevlerin: ... (SSE formatında akar)
```

### RAG — Doküman Yükleme
```bash
curl -X POST http://localhost:8080/documents/upload \
  -F "file=@test-documents/proje-dokumani.txt"
```

### H2 Console (veritabanını görüntüle)
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:tododb
```

### MCP Server — Claude Code Entegrasyonu
Uygulama çalışırken `.claude/settings.json`'a ekle:
```json
{
  "mcpServers": {
    "todo": {
      "url": "http://localhost:8080/mcp"
    }
  }
}
```
Claude Code bu proje üzerinde çalışırken TODO araçlarını doğrudan kullanabilir.

## Mevcut Araçlar (Tools)

| Tool | Açıklama |
|------|----------|
| `createTodo` | Yeni görev oluştur (title, priority, dueDate, description) |
| `listTodos` | Görevleri listele (ALL / PENDING / COMPLETED filtresi) |
| `completeTodo` | Görevi tamamlandı olarak işaretle (ID ile) |
| `searchTodos` | Anahtar kelimeyle arama |
| `deleteTodo` | Görevi sil (ID ile) |
| `getCurrentDateTime` | Şu anki tarih/saat |

## Agent Nasıl Çalışır?

```
Kullanıcı: "Sprint review'u yarın için yüksek öncelikli ekle"
    ↓
LLM: getCurrentDateTime() çağır → "2026-04-21"
    ↓
LLM: createTodo("Sprint review", "HIGH", "2026-04-22", null) çağır
    ↓
LLM: "Sprint review görevi yarın için oluşturuldu."
```

Loglardan (`logging.level.org.springframework.ai: DEBUG`) her tool çağrısını takip edebilirsin.

## Tamamlanan Özellikler

- [x] Tool Calling (`@Tool` annotation)
- [x] RAG (doküman yükleme + semantik arama)
- [x] Agent Memory (session bazlı konuşma geçmişi)
- [x] Streaming response (`Flux<String>` + SSE)
- [x] Web UI (markdown render, doküman yükleme paneli)
- [x] MCP Server
