package com.pudding.agentscope.agentscopedemo.demo;

import com.pudding.agentscope.agentscopedemo.tools.WeatherTool;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.*;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.UserMessage;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.core.permission.PermissionContextState;
import io.agentscope.core.permission.PermissionMode;
import io.agentscope.core.state.JsonFileAgentStateStore;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;

public class AgentEventDemo {
    static void main() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTool());
        toolkit.registerTool(new WriteFileTool());
        toolkit.registerTool(new ReadFileTool());

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
                // ═══════════════════════════════════════════════════════════
                // 【重要】如果不写 .stateStore(...)，agent 的对话记忆、工具调用
                // 状态全部存在 JVM 堆内存里。进程一结束就全丢了，磁盘上不会
                // 产生任何 .agentscope 目录或文件——"哪里也不去，纯内存。"
                //
                // 当前配置了 JsonFileAgentStateStore，持久化到磁盘：
                //   .agentscope/workspace/{userId目录}/pudding001/
                //   ● agent_state.json     —— Agent 整体状态
                //   ● memory_messages.jsonl —— 对话历史（JSONL 增量追加）
                //   userId 为 null 时目录名是 __anon__
                // ═══════════════════════════════════════════════════════════
                .stateStore(new JsonFileAgentStateStore(Path.of(".agentscope", "workspace")))
                .build();

        // 构建带 userId 的 RuntimeContext，用于隔离不同用户的会话状态
        RuntimeContext ctx = RuntimeContext.builder()
                .userId("pudding")        // 当前用户标识，存入 stateStore 时作为目录名
                .sessionId("pudding001")  // 会话标识，与 defaultSessionId 保持一致
                .build();

        // 使用带 userId/sessionId 的 RuntimeContext 发起流式调用
        agent.streamEvents(new UserMessage("上海的天气怎么样？"), ctx)
                .doOnNext(event -> {
                    if (event instanceof AgentStartEvent start) {
                        System.out.println("[start replyId=" + start.getReplyId() + "]");
                    } else if (event instanceof TextBlockDeltaEvent delta) {
                        System.out.print(delta.getDelta());
                    } else if (event instanceof ToolCallStartEvent tc) {
                        System.out.println("\n[正在调用 " + tc.getToolCallName() + "...]");
                    } else if (event instanceof ToolResultEndEvent end) {
                        System.out.println("[工具执行完成：" + end.getState() + "]");
                    } else if (event instanceof AgentEndEvent end) {
                        System.out.println("\n[完成]");
                    }
                })
                .blockLast();
    }
}
