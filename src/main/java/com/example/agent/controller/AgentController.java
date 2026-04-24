package com.example.agent.controller;

import com.example.agent.service.AgentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId()
                : UUID.randomUUID().toString();

        String response = agentService.chat(request.message(), sessionId);
        return new ChatResponse(response, sessionId);
    }

    /**
     * Streaming endpoint: token token Server-Sent Events olarak döner.
     * UI bu endpoint'i fetch + ReadableStream ile okur.
     *
     * Neden ayrı endpoint?
     *   /chat      → tek seferde JSON cevap (senkron)
     *   /chat/stream → token akışı (asenkron, SSE)
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId()
                : UUID.randomUUID().toString();

        return agentService.chatStream(request.message(), sessionId);
    }

    record ChatRequest(String message, String sessionId) {}
    record ChatResponse(String response, String sessionId) {}
}
