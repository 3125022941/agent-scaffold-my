package org.example.domain.agent.service.armory.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.agent.model.entity.ArmoryCommandEntity;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.armory.node.RootNode;
import org.jvnet.hk2.annotations.Service;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.HashMap;
import java.util.Map;
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

        private Map<String,Object>dataObjects=new HashMap<>();

        public <T>void setValue(String key,T value){dataObjects.put(key,value);}

        public <T> T getValue(String key){return (T)dataObjects.get(key);}

    }
}
