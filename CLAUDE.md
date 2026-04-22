# Smart TODO Agent — POC Projesi

AI Agent + MCP Server konseptlerini öğrenmek için Spring Boot + Spring AI ile yazılmış POC.

## Proje Amacı

Bu proje şu kavramları uygulamalı olarak öğretmek için tasarlanmıştır:
- **Spring AI Tool Calling**: `@Tool` annotation ile LLM'e Java method'larını araç olarak sunma
- **AI Agent döngüsü**: LLM'in otomatik tool seçimi ve multi-step reasoning
- **MCP Server**: Araçları dışarıya açma (Claude Desktop/Code bağlanabilir)
- **Model değişimi**: Claude ↔ Ollama/Gemma4 swap sadece `application.yml` ile

## Proje Yapısı

```
src/main/java/com/example/agent/
├── SmartTodoAgentApplication.java   — Spring Boot entry point
├── controller/
│   └── AgentController.java         — POST /chat REST endpoint
├── service/
│   └── AgentService.java            — ChatClient konfigürasyonu ve agent mantığı
├── tools/
│   └── TodoTools.java               — 6 adet @Tool annotated method (agent araçları)
├── entity/
│   └── Todo.java                    — JPA entity: title, priority, status, dueDate
└── repository/
    └── TodoRepository.java          — Spring Data JPA
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

### REST API — Chat Endpoint
```bash
# Görev ekle
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Login bug fix taskını yüksek öncelikli ekle, son tarih 30 Nisan"}'

# Görevleri listele
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Bugün ne yapmalıyım?"}'

# Tamamla
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "1 numaralı görevi tamamlandı olarak işaretle"}'
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

## Sonraki Adımlar

- [ ] Çoklu agent: Planner + Executor agent mimarisi
- [ ] RAG: Doküman yükleme + vektör veritabanı
- [ ] Konuşma geçmişi: Session-based memory
- [ ] Web UI: Basit chat arayüzü
