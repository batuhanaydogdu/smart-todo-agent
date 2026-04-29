package com.example.agent.dto;

/**
 * LLM'in metinden çıkardığı tek bir TODO görevi.
 *
 * Spring AI .entity() çağrısında bu record'un field'larından
 * otomatik JSON schema üretir ve LLM'e gönderir.
 * LLM JSON döner, Jackson otomatik deserialize eder.
 */
public record ExtractedTodo(
        String title,       // Görev başlığı
        String priority,    // LOW / MEDIUM / HIGH — LLM metinden çıkarır
        String dueDate,     // YYYY-MM-DD formatında, çıkarılamazsa null
        String description, // Ek bağlam, opsiyonel
        String reasoning    // LLM neden bu önceliği seçti — öğretici amaçlı
) {}
