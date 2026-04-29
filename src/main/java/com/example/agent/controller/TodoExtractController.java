package com.example.agent.controller;

import com.example.agent.dto.ExtractTodosResponse;
import com.example.agent.dto.ExtractedTodo;
import com.example.agent.entity.Todo;
import com.example.agent.repository.TodoRepository;
import com.example.agent.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Structured Output endpoint'leri.
 *
 * POST /todos/extract       → Metinden todo listesi çıkar (önizleme, kaydetmez)
 * POST /todos/extract/save  → Çıkarılan todo'ları DB'ye kaydet
 *
 * Akış:
 *   1. Kullanıcı metin yapıştırır → /extract → LLM parse eder → önizleme gösterilir
 *   2. Kullanıcı onaylar → /extract/save → seçilenler DB'ye kaydedilir
 */
@RestController
@RequestMapping("/todos")
public class TodoExtractController {

    private final AgentService agentService;
    private final TodoRepository todoRepository;

    public TodoExtractController(AgentService agentService, TodoRepository todoRepository) {
        this.agentService = agentService;
        this.todoRepository = todoRepository;
    }

    /**
     * Metinden todo çıkar — sadece önizleme, DB'ye yazmaz.
     * LLM .entity() ile tip-güvenli obje döner.
     */
    @PostMapping("/extract")
    public ResponseEntity<ExtractTodosResponse> extract(@RequestBody ExtractRequest request) {
        if (request.text() == null || request.text().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        ExtractTodosResponse response = agentService.extractTodos(request.text());
        return ResponseEntity.ok(response);
    }

    /**
     * Seçilen todo'ları DB'ye kaydet.
     * Kullanıcı önizlemede istediği todo'ları seçip onaylar.
     */
    @PostMapping("/extract/save")
    public ResponseEntity<SaveResponse> save(@RequestBody List<ExtractedTodo> todos) {
        int saved = 0;
        for (ExtractedTodo extracted : todos) {
            Todo todo = new Todo();
            todo.setTitle(extracted.title());
            todo.setDescription(extracted.description());

            // Priority parse — LLM bazen küçük harf veya farklı format döndürebilir
            try {
                todo.setPriority(Todo.Priority.valueOf(extracted.priority().toUpperCase()));
            } catch (Exception e) {
                todo.setPriority(Todo.Priority.MEDIUM);
            }

            // Tarih parse — LLM YYYY-MM-DD formatında dönmeli
            if (extracted.dueDate() != null && !extracted.dueDate().isBlank()) {
                try {
                    todo.setDueDate(LocalDate.parse(extracted.dueDate()));
                } catch (Exception ignored) {}
            }

            todoRepository.save(todo);
            saved++;
        }
        return ResponseEntity.ok(new SaveResponse(saved, saved + " görev kaydedildi."));
    }

    record ExtractRequest(String text) {}
    record SaveResponse(int count, String message) {}
}
