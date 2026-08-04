package org.example.domain.agent.service.armory.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jvnet.hk2.annotations.Service;

import java.util.HashMap;
import java.util.Map;
@Service
public class DefaultArmoryFactory {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicContext{
        private Map<String,Object>dataObjects=new HashMap<>();
        public <T>void setValue(String key,T value){
            dataObjects.put(key,value);
        }
        public <T> T getValue(String key){
            return (T)dataObjects.get(key);
        }
    }
}
