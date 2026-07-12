package com.pudding.agentscope.agentscopedemo.tools;

import com.pudding.agentscope.agentscopedemo.demo.RunTimeContextDemo;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

public class WeatherTool {

    @Tool(name = "getWeather", description = "Get the weather in a city")
    public String getWeather(@ToolParam(name = "city", description = "The city to check the weather for") String city,
                             RunTimeContextDemo.UserContext userContext, RuntimeContext runtimeContext
    ) {
        return "The weather in " + city + " is sunny.";
    }
}
