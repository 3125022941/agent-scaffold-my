package org.example.domain.agent.service.armory.node.workflow;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.ParallelAgent;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.AbstractArmorySupport;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.jvnet.hk2.annotations.Service;

import java.util.List;

@Slf4j
@Service("parallelAgentNode")
public class ParallelAgentNode extends AbstractArmorySupport {
    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai AGENT 装配操作 -parallelAgentNode");

        List<AIAgentConfigTableVO.Module.AgentWorkflow>agentWorkflows=dynamicContext.getAgentWorkflows();
        AIAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.remove(0);

        List<String>subAgent=agentWorkflow.getSubAgents();
        List<BaseAgent> subAgents = dynamicContext.queryAgentList(subAgent);

        ParallelAgent parallelAgent=
                ParallelAgent.builder()
                        .name(agentWorkflow.getName())
                        .description(agentWorkflow.getDescription())
                        .subAgents(subAgents)
                        .build();
        dynamicContext.getAgentGroup().put(agentWorkflow.getName(),parallelAgent);
        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        List<AIAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows = dynamicContext.getAgentWorkflows();
        if(null==agentWorkflows||agentWorkflows.isEmpty()){
            return defaultStrategyHandler;
        }
        AIAgentConfigTableVO.Module.AgentWorkflow agentWorkflow = agentWorkflows.get(0);

        String type = agentWorkflow.getType();
        AgentTypeEnum agentTypeEnum=AgentTypeEnum.formType(type);
        if(null==agentTypeEnum){
            throw new RuntimeException("agentWork type is error!");
        }
        String node=agentTypeEnum.getNode();

        return switch (node){

            case "loopAgentNode" ->getBean("loopAgentNode");
            case "sequentialAgentNode" ->getBean("sequentialAgentNode");
            default -> defaultStrategyHandler;
        };
    }
}
