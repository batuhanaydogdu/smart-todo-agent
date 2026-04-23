package com.example.agent.tools;

import com.example.agent.entity.Todo;
import com.example.agent.repository.TodoRepository;
import com.example.agent.service.DocumentService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bu class'taki her method bir AI tool'udur.
 * LLM kullanıcının isteğini anlayıp uygun tool'u otomatik çağırır.
 * Aynı tool'lar MCP server üzerinden de dışarıya açılır.
 */
@Component
public class TodoTools {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private DocumentService documentService;

    @Tool(description = "Yeni bir TODO görevi oluştur. title: görev başlığı, " +
                        "priority: LOW/MEDIUM/HIGH (varsayılan MEDIUM), " +
                        "dueDate: son tarih YYYY-MM-DD formatında (opsiyonel), " +
                        "description: ek açıklama (opsiyonel)")
    public String createTodo(String title, String priority, String dueDate, String description) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);

        try {
            todo.setPriority(Todo.Priority.valueOf(priority.toUpperCase()));
        } catch (Exception e) {
            todo.setPriority(Todo.Priority.MEDIUM);
        }

        if (dueDate != null && !dueDate.isBlank()) {
            try {
                todo.setDueDate(LocalDate.parse(dueDate));
            } catch (Exception ignored) {}
        }

        Todo saved = todoRepository.save(todo);
        return "TODO oluşturuldu: [ID=" + saved.getId() + "] " + saved.getTitle() +
               " (Öncelik: " + saved.getPriority() + ")";
    }

    @Tool(description = "Mevcut TODO görevlerini listele. " +
                        "status filtresi: ALL (hepsi), PENDING (bekleyenler), COMPLETED (tamamlananlar). " +
                        "Öncelik sırasına göre sıralanır.")
    public String listTodos(String status) {
        List<Todo> todos;

        if ("COMPLETED".equalsIgnoreCase(status)) {
            todos = todoRepository.findByStatus(Todo.Status.COMPLETED);
        } else if ("PENDING".equalsIgnoreCase(status)) {
            todos = todoRepository.findByStatus(Todo.Status.PENDING);
        } else {
            todos = todoRepository.findByOrderByPriorityDescCreatedAtAsc();
        }

        if (todos.isEmpty()) {
            return "Hiç TODO bulunamadı.";
        }

        return todos.stream()
            .map(t -> String.format("[ID=%d] %s | Öncelik: %s | Durum: %s%s",
                t.getId(), t.getTitle(), t.getPriority(), t.getStatus(),
                t.getDueDate() != null ? " | Son tarih: " + t.getDueDate() : ""))
            .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Belirtilen ID'li TODO görevini tamamlandı olarak işaretle.")
    public String completeTodo(Long id) {
        return todoRepository.findById(id).map(todo -> {
            todo.setStatus(Todo.Status.COMPLETED);
            todoRepository.save(todo);
            return "TODO tamamlandı: [ID=" + id + "] " + todo.getTitle();
        }).orElse("ID=" + id + " olan TODO bulunamadı.");
    }

    @Tool(description = "TODO görevlerinde anahtar kelimeyle arama yap. " +
                        "Başlık ve açıklamada arama yapılır.")
    public String searchTodos(String keyword) {
        List<Todo> results = todoRepository.searchByKeyword(keyword);

        if (results.isEmpty()) {
            return "'" + keyword + "' için sonuç bulunamadı.";
        }

        return results.stream()
            .map(t -> String.format("[ID=%d] %s | Öncelik: %s | Durum: %s",
                t.getId(), t.getTitle(), t.getPriority(), t.getStatus()))
            .collect(Collectors.joining("\n"));
    }

    @Tool(description = "Belirtilen ID'li TODO görevini sil.")
    public String deleteTodo(Long id) {
        if (!todoRepository.existsById(id)) {
            return "ID=" + id + " olan TODO bulunamadı.";
        }
        todoRepository.deleteById(id);
        return "TODO silindi: ID=" + id;
    }

    @Tool(description = "Şu anki tarih ve saati döndür. Tarih bazlı sorgular için kullan.")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "Yüklenen dokümanlarda semantik arama yap. " +
                        "Kullanıcı bir konu hakkında bilgi sorarsa, " +
                        "daha önce /documents/upload ile yüklenen dosyalarda bu tool ile ara. " +
                        "TODO yönetimiyle ilgisi olmayan bilgi sorularında bu tool'u kullan.")
    public String searchDocuments(String query) {
        return documentService.search(query);
    }
}
