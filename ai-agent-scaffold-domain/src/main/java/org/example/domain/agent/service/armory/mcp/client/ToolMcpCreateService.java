package org.example.domain.agent.service.armory.mcp.client;

import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.springframework.ai.tool.ToolCallback;

public interface ToolMcpCreateService {
    ToolCallback[] buildToolCallback(AIAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) throws Exception;
}
