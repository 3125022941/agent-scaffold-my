package org.example.domain.agent.service.armory.matter.mcp.client.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.mcp.client.ToolMcpCreateService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocalToolMcpCreateService implements ToolMcpCreateService {

    @Resource
    private ApplicationContext applicationContext;

    @Override
    public ToolCallback[] buildToolCallback(AIAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) {
        AIAgentConfigTableVO.Module.ChatModel.LocalParameters local = toolMcp.getLocal();
        String name = local.getName();
        ToolCallbackProvider localToolCallbackProvider = applicationContext.getBean(name, ToolCallbackProvider.class);
        log.info("tool local mcp initialize: {}", name);
        return localToolCallbackProvider.getToolCallbacks();
    }
}
