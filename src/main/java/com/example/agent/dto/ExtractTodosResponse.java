package com.example.agent.dto;

import java.util.List;

/**
 * Structured Output response'u — LLM'in metinden çıkardığı tüm görevler.
 *
 * Bu class Spring AI'ın .entity(ExtractTodosResponse.class) çağrısıyla
 * otomatik olarak doldurulur. LLM bu schema'ya uymak zorunda kalır.
 */
public record ExtractTodosResponse(
        List<ExtractedTodo> todos,  // Çıkarılan görevler listesi
        String sourceType,          // MEETING_NOTES / EMAIL / SLACK / OTHER
        int todoCount,              // Kaç görev bulundu
        String warning              // Acil deadline varsa uyarı mesajı, yoksa null
) {}
