package org.example.domain.agent.service.armory.matter.mcp.client.factory;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.mcp.client.ToolMcpCreateService;
import org.example.domain.agent.service.armory.matter.mcp.client.impl.LocalToolMcpCreateService;
import org.example.domain.agent.service.armory.matter.mcp.client.impl.SSEToolMcpCreateService;
import org.example.domain.agent.service.armory.matter.mcp.client.impl.StdioToolMcpCreateService;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DefaultMcpClientFactory {
    @Resource
    private LocalToolMcpCreateService localToolMcpCreateService;
    @Resource
    private SSEToolMcpCreateService sseToolMcpCreateService;
    @Resource
    private StdioToolMcpCreateService stdioToolMcpCreateService;

    public ToolMcpCreateService getToolMcpCreateService(AIAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) {
        if(null!=toolMcp.getLocal())return localToolMcpCreateService;
        if(null!=toolMcp.getSse())return sseToolMcpCreateService;
        if(null!=toolMcp.getStdio())return stdioToolMcpCreateService;
        throw new AppException(ResponseCode.NOT_FOUND_METHOD.getCode(),ResponseCode.NOT_FOUND_METHOD.getInfo());

    }
}
