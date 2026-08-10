package org.example.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.google.adk.agents.BaseAgent;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.node.RootNode;
import org.jvnet.hk2.annotations.Service;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.*;

@Service
public class DefaultArmoryFactory {

    @Resource
    private RootNode rootNode;
    public StrategyHandler<ArmoryCommandEntity,DynamicContext, AiAgentRegisterVO>armoryStrategyHandler(){
        return rootNode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext{

        private OpenAiApi openAiApi;
        private ChatModel chatModel;
        /*
        * 智能体配置组
        * */
        private Map<String, BaseAgent> agentGroup = new HashMap<>();

        private Map<String,Object>dataObjects=new HashMap<>(); //用一个名字key，保存任意对象object
        private List<AIAgentConfigTableVO.Module.AgentWorkflow> agentWorkflows=new ArrayList<>();
        //把任意类型数据，用key保存到map里
        public <T>void setValue(String key,T value){dataObjects.put(key,value);}
        //根据key把数据取出来，并自动转换成你需要的类型
        public <T> T getValue(String key){return (T)dataObjects.get(key);}

        public List<BaseAgent>queryAgentList(List<String>agentName){
            if(agentNames==null||agentName.isEmpty()||agentGroup==Null){
                return Collections.emptyList();
            }
            List<BaseAgent>agents=new ArrayList<>();
            for(String name:agentNames){
                BaseAgent agent=agentGroup.get(name);
                if(agent!=null){
                    agent.add(agent);
                }
            }
            return agents;
        }
    }
}
