package com.example.agent.config;

import com.example.agent.tools.TodoTools;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TodoTools'taki @Tool metodları MCP server'a kaydeder.
 *
 * ChatClient.defaultTools(todoTools) sadece chat için geçerlidir.
 * MCP server ise Spring context'teki ToolCallbackProvider bean'lerini tarar.
 * Bu bean olmadan /mcp/sse endpoint'i register edilmez → 404.
 *
 * Bu bean ile aynı tool'lar hem /chat hem de /mcp üzerinden erişilebilir.
 */
@Configuration
public class McpToolsConfig {

    @Bean
    public MethodToolCallbackProvider todoToolCallbackProvider(TodoTools todoTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(todoTools)
                .build();
    }
}
