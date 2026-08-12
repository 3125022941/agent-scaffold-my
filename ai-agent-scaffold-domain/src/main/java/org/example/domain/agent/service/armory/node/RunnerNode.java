package org.example.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.plugins.BasePlugin;
import com.google.adk.runner.InMemoryRunner;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.AbstractArmorySupport;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RunnerNode extends AbstractArmorySupport {
    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai AGENT 装配操作 -RunnerNode");
        AIAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        String appName = aiAgentConfigTableVO.getAppName();
        AIAgentConfigTableVO.Agent agent = aiAgentConfigTableVO.getAgent();
        String agentId = agent.getAgentId();
        String agentName = agent.getAgentName();
        String agentDesc = agent.getAgentDesc();

        InMemoryRunner runner = getRunner(dynamicContext, aiAgentConfigTableVO, appName);

        AiAgentRegisterVO aiAgentRegisterVO = AiAgentRegisterVO.builder()
                .appName(appName)
                .agentId(agentId)
                .agentName(agentName)
                .agentDesc(agentDesc)
                .runner(runner)
                .build();
        //注册到spring容器
        registerBean(agentId, AiAgentRegisterVO.class, aiAgentRegisterVO);

        return aiAgentRegisterVO;
    }

    private @NonNull InMemoryRunner getRunner(DefaultArmoryFactory.DynamicContext dynamicContext, AIAgentConfigTableVO aiAgentConfigTableVO, String appName) {
        AIAgentConfigTableVO.Module.Runner runnerConfig= aiAgentConfigTableVO.getModule().getRunner();

        String agentName=runnerConfig.getAgentName();
        if(StringUtils.isBlank(agentName)){
            log.error("runner.agentName is null");
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(),ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }

        BaseAgent baseAgent= dynamicContext.getAgentGroup().get(runnerConfig.getAgentName());
        List<BasePlugin>plugins;
        List<String>pluginNameList= runnerConfig.getPluginNameList();
        if (pluginNameList != null && !pluginNameList.isEmpty()) {
            plugins=new ArrayList<>();
            for(String pluginName :pluginNameList){
                BasePlugin plugin=getBean(pluginName);
                plugins.add(plugin);
            }
        }else {
            plugins=ImmutableList.of();
        }
        return new InMemoryRunner(baseAgent, appName, plugins);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
