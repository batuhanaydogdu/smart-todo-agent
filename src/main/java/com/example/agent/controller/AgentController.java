package com.example.agent.controller;

import com.example.agent.service.AgentService;
import org.springframework.web.bind.annotation.*;

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
        // sessionId gelmezse UUID üret — her istek ayrı oturum olur
        String sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
                ? request.sessionId()
                : UUID.randomUUID().toString();

        String response = agentService.chat(request.message(), sessionId);
        return new ChatResponse(response, sessionId);
    }

    record ChatRequest(String message, String sessionId) {}
    record ChatResponse(String response, String sessionId) {}
}
