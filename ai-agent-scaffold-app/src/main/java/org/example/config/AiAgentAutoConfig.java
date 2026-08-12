package org.example.config;

import jakarta.annotation.Resource;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.valobj.properties.AiAgentAutoConfigPropertes;
import org.example.domain.agent.service.IArmoryService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Slf4j //自动创建日志对象
@Configuration //让spring发现这个配置类
@EnableConfigurationProperties(AiAgentAutoConfigPropertes.class)//让配置文件绑定到属性对象
public class AiAgentAutoConfig implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private AiAgentAutoConfigPropertes aiAgentAutoConfigPropertes;
    @Resource
    private IArmoryService armoryService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event){
        try{
            log.info("Ai Agent 智能体装配{}", JSON.toJSONString(aiAgentAutoConfigPropertes.getTables().values()));

            armoryService.acceptArmoryAgents(new ArrayList<>(aiAgentAutoConfigPropertes.getTables().values()));
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
