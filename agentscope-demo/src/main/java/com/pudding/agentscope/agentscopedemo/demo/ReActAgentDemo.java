package com.pudding.agentscope.agentscopedemo.demo;

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

public class ReActAgentDemo {
    static void main() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherTool());
        toolkit.registerTool(new WriteFileTool());
        toolkit.registerTool(new ReadFileTool());

        PermissionContextState permissionContextState = PermissionContextState.builder()
                .mode(PermissionMode.DEFAULT)
                .build();

        ReActAgent agent = ReActAgent.builder()
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
                .build();

        // 查看空name状态
        System.out.println("Agent Name：" + agent.getName());
        System.out.println("Default SessionId：" + agent.getDefaultSessionId());

        // 方式一
        // Msg block = agent.call(Msg.builder().textContent("你好，你知道我的爱好吗？").build()).block();

        // 方式二
        Msg block = agent.call(new UserMessage("你好，上海的天气怎么样？")).block();
        System.out.println(block.getTextContent());
    }
}
