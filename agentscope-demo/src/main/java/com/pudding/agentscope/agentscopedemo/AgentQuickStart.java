package com.pudding.agentscope.agentscopedemo;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;

import java.nio.file.Paths;

public class AgentQuickStart {
    static void main() {
        stream();
    }

    private static void stream() {
        HarnessAgent agent = HarnessAgent.builder()
                .name("MyAgent")
                .model(OpenAIChatModel.builder()
                        .baseUrl("https://api.deepseek.com")
                        .modelName("deepseek-v4-pro")
                        .apiKey("sk-3928c9ff735d48f59f3f4586f421a593")
                        .build())
                // 字符串形式由 ModelRegistry 解析 —— 自动读取 DASHSCOPE_API_KEY；
                // 切换其他厂商时改用 "openai:gpt-5.5"、"anthropic:claude-sonnet-4-5"、
                // "gemini:gemini-2.0-flash" 或 "ollama:llama3"。
                // .model("deepseek:deepseek-v4-pro")
                .workspace(Paths.get(".agentscope/workspace"))
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build();

        RuntimeContext context = RuntimeContext.builder()
                .userId("user123")
                .sessionId("session1")
                .build();

        agent.streamEvents(Msg.builder().textContent("你好，你知道我的爱好吗？").build(), context)
                .doOnNext(event -> {
                    // 方式一
                    // if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA) {
                    //     // 模型返回的流式文本片段 —— 追加到界面或标准输出
                    //     System.out.print(((TextBlockDeltaEvent) event).getDelta());
                    // } else if (event.getType() == AgentEventType.TOOL_CALL_START) {
                    //     // 智能体即将调用工具 —— 展示调用信息
                    //     System.out.println("\n[tool] " + ((ToolCallStartEvent) event).getToolCallName());
                    // }
                    // 其他事件：思考块、工具结果、回复结束等

                    // 方式二
                    if (event instanceof TextBlockDeltaEvent deltaEvent) {
                        System.out.print(deltaEvent.getDelta());
                    }
                }).blockLast();

    }

    private static void call() {
        HarnessAgent agent = HarnessAgent.builder()
                .name("MyAgent")
                .model(OpenAIChatModel.builder()
                        .baseUrl("https://api.deepseek.com")
                        .modelName("deepseek-v4-pro")
                        .apiKey("sk-3928c9ff735d48f59f3f4586f421a593")
                        .build())
                // 字符串形式由 ModelRegistry 解析 —— 自动读取 DASHSCOPE_API_KEY；
                // 切换其他厂商时改用 "openai:gpt-5.5"、"anthropic:claude-sonnet-4-5"、
                // "gemini:gemini-2.0-flash" 或 "ollama:llama3"。
                // .model("deepseek:deepseek-v4-pro")
                .workspace(Paths.get(".agentscope/workspace"))
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build();

        RuntimeContext context = RuntimeContext.builder()
                .userId("user123")
                .sessionId("session1")
                .build();

        Msg block = agent.call(Msg.builder().textContent("你好，你知道我的爱好吗？").build(), context).block();
        System.out.println(block.getTextContent());
    }
}
