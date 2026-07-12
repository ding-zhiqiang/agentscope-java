package com.pudding.agentscope.agentscopedemo.demo;

import com.pudding.agentscope.agentscopedemo.tools.WeatherTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.permission.*;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.core.tool.Toolkit;

import java.nio.file.Path;
import java.util.List;

public class PermissionContextDemo {

    public record UserContext(String name, Integer age) {
    }

    static void main() {
        PermissionContextState permissionContextState = PermissionContextState.builder()
                .mode(PermissionMode.DEFAULT)
                .addAskRule("dangerous_delete",
                        new PermissionRule("dangerous_delete", null, PermissionBehavior.ASK, "userSettings"))
                .build();

        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTool());

        ReActAgent agent = ReActAgent.builder()
                .name("ReActAgentDemo")
                .sysPrompt("你是一个智能的助手，需要根据用户的问题，给出详细的回答。")
                .model(OpenAIChatModel.builder()
                        .baseUrl("https://api.deepseek.com")
                        .modelName("deepseek-v4-pro")
                        .apiKey("sk-3928c9ff735d48f59f3f4586f421a593")
                        .build())
                .defaultSessionId("pudding001") // 未传 RuntimeContext 时，sessionId 的兜底值
                .toolkit(toolkit)
                .stateStore(new JsonFileAgentStateStore(Path.of(".agentscope", "workspace")))
                .permissionContext(permissionContextState)
                .build();

        // 构建带 userId 的 RuntimeContext，用于隔离不同用户的会话状态
        RuntimeContext ctx = RuntimeContext.builder()
                .userId("pudding")        // 当前用户标识，存入 stateStore 时作为目录名
                .sessionId("001")  // 会话标识，与 defaultSessionId 保持一致
                .put("name", "pudding")
                .put(UserContext.class, new UserContext("puddingcode", 18))
                .build();

        // 使用带 userId/sessionId 的 RuntimeContext 发起流式调用
        UserMessage userMessage = new UserMessage("user", "上海的天气怎么样？");
        Msg block = agent.call(List.of(userMessage), ctx).block();
        System.out.println(block.getTextContent());
    }
}
