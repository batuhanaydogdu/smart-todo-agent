package com.example.agent.controller;

import com.example.agent.service.AgentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String response = agentService.chat(request.message());
        return new ChatResponse(response);
    }

    record ChatRequest(String message) {
    }

    record ChatResponse(String response) {
    }
}
