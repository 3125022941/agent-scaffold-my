package org.example.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.AbstractArmorySupport;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.example.domain.agent.service.armory.matter.mcp.client.ToolMcpCreateService;
import org.example.domain.agent.service.armory.matter.mcp.client.factory.DefaultMcpClientFactory;
import org.example.domain.agent.service.armory.matter.skills.ToolSkillsCreateService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChatModelNode extends AbstractArmorySupport {
    @Resource
    private AgentNode agentNode;
    @Resource
    private DefaultMcpClientFactory defaultMcpClientFactory;
    @Resource
    private ToolSkillsCreateService toolSkillsCreateService;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配工作 - ChatModelNode");
        //获取上下文对象
        OpenAiApi openAiApi = dynamicContext.getOpenAiApi();
        //获取配置对象
        AIAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        AIAgentConfigTableVO.Module.ChatModel chatModelConfig = aiAgentConfigTableVO.getModule().getChatModel();

        //构建mcp服务（工厂）
        List<ToolCallback> toolCallbackList = new ArrayList<>();
        List<AIAgentConfigTableVO.Module.ChatModel.ToolMcp> toolMcpList = chatModelConfig.getToolMcpList();
        List<AIAgentConfigTableVO.Module.ChatModel.ToolSkills> toolSkillsList = chatModelConfig.getToolSkillsList();

        if (toolMcpList != null) {
            for (AIAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp : toolMcpList) {
                try {
                    ToolMcpCreateService toolMcpCreateService = defaultMcpClientFactory.getToolMcpCreateService(toolMcp);
                    ToolCallback[] toolCallbacks = toolMcpCreateService.buildToolCallback(toolMcp);
                    toolCallbackList.addAll(List.of(toolCallbacks));
                } catch (Exception e) {
                    log.warn("跳过不可用的 MCP 工具，模型将继续启动: {}", e.getMessage());
                }
            }
        }
        if (toolSkillsList != null) {
            for (AIAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills : toolSkillsList) {
                ToolCallback[] toolCallbacks = toolSkillsCreateService.buildToolCallback(toolSkills);
                toolCallbackList.addAll(List.of(toolCallbacks));
            }
        }
       //构建对话模型
        ChatModel chatModel= OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(chatModelConfig.getModel())
                        .toolCallbacks(toolCallbackList)
                        .build())
                .build();
        dynamicContext.setChatModel(chatModel);

        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return agentNode;
    }





}
