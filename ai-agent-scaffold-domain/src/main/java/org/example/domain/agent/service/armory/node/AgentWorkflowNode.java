package org.example.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.model.valobj.enums.AgentTypeEnum;
import org.example.domain.agent.service.armory.AbstractArmorySupport;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.example.domain.agent.service.armory.node.workflow.LoopAgentNode;
import org.example.domain.agent.service.armory.node.workflow.ParallelAgentNode;
import org.example.domain.agent.service.armory.node.workflow.SequentialAgentNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AgentWorkflowNode extends AbstractArmorySupport {
    @Resource
    private LoopAgentNode loopAgentNode;
    @Resource
    private ParallelAgentNode parallelAgentNode;
    @Resource
    private SequentialAgentNode sequentialAgentNode;
    @Resource
    private RunnerNode runnerNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent 装配操作 -AgentNode");
        AIAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        List<AIAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows =aiAgentConfigTableVO.getModule().getAgentWorkflows();

        if (null==agentWorkflows||agentWorkflows.isEmpty()||dynamicContext.getCurrentStepIndex()>=agentWorkflows.size()){
            dynamicContext.setCurrentAgentWorkflow(null);
            return router(requestParameter,dynamicContext);
        }
        dynamicContext.setCurrentAgentWorkflow(agentWorkflows.get(dynamicContext.getCurrentStepIndex()));
        //步骤值增加
        dynamicContext.addCurrentStepIndex();
        return router(requestParameter,dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        AIAgentConfigTableVO.Module.AgentWorkflow currentAgentWorkflow=dynamicContext.getCurrentAgentWorkflow();
        if (null==currentAgentWorkflow){
            return runnerNode;
        }


        String type = currentAgentWorkflow.getType();
        AgentTypeEnum agentTypeEnum=AgentTypeEnum.formType(type);
        if(null==agentTypeEnum){
            throw new RuntimeException("agentWork type is error!");
        }
        String node=agentTypeEnum.getNode();

        return switch (node){
            case "loopAgentNode" -> loopAgentNode;
            case "parallelAgentNode" ->parallelAgentNode;
            case "sequentialAgentNode" ->sequentialAgentNode;
            default -> runnerNode;
        };
    }
}
