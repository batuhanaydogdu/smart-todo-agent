package com.example.agent.controller;

import com.example.agent.service.AgentService;
import com.example.agent.service.SessionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class AgentController {

    private final AgentService agentService;
    private final SessionService sessionService;

    public AgentController(AgentService agentService, SessionService sessionService) {
        this.agentService = agentService;
        this.sessionService = sessionService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        sessionService.touchSession(sessionId);
        String response = agentService.chat(request.message(), sessionId);
        return new ChatResponse(response, sessionId);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        String sessionId = resolveSessionId(request.sessionId());
        sessionService.touchSession(sessionId);
        return agentService.chatStream(request.message(), sessionId);
    }

    private String resolveSessionId(String sessionId) {
        return (sessionId != null && !sessionId.isBlank())
                ? sessionId
                : UUID.randomUUID().toString();
    }

    record ChatRequest(String message, String sessionId) {}
    record ChatResponse(String response, String sessionId) {}
}
