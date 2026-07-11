package com.pudding.agentscope.agentscopedemo.config;

import com.pudding.agentscope.agentscopedemo.tools.WeatherTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public ReActAgent reActAgent() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTool());
        toolkit.registerTool(new WriteFileTool());
        toolkit.registerTool(new ReadFileTool());

        PermissionContextState permissionContextState = PermissionContextState.builder()
                .mode(PermissionMode.DEFAULT)
                .build();

        return ReActAgent.builder()
                .name("ReActAgentDemo")
                .sysPrompt("你是一个智能的助手，需要根据用户的问题，给出详细的回答。")
                .model(OpenAIChatModel.builder()
                        .baseUrl("https://api.deepseek.com")
                        .modelName("deepseek-v4-pro")
                        .apiKey("sk-3928c9ff735d48f59f3f4586f421a593")
                        .build())
                .defaultSessionId("pudding001")
                .permissionContext(permissionContextState)
                .toolkit(toolkit)
                .maxIters(10) // 最大迭代次数
                .build();
    }
}
