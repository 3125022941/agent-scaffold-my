package org.example.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.AbstractArmorySupport;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.jvnet.hk2.annotations.Service;
import org.springframework.ai.openai.api.OpenAiApi;

@Slf4j
@Service
public class AiApiNode extends AbstractArmorySupport {

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 - AiApiNode");
        AIAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        AIAgentConfigTableVO.Module.AiApi aiApiConfig = aiAgentConfigTableVO.getModule().getAiApi();

        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(aiApiConfig.getBaseUrl())
                .apiKey(aiApiConfig.getApiKey())
                .completionsPath(StringUtils.isNoneBlank(aiApiConfig.getCompletionsPath())?aiApiConfig.getCompletionsPath():"v1/chat/completions")
                .embeddingsPath(StringUtils.isNoneBlank(aiApiConfig.getEmbeddingsPath())?aiApiConfig.getEmbeddingsPath():"v1/embeddings")
                .build();
        dynamicContext.setOpenAiApi(openAiApi);
        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
