package org.example.domain.agent.model.valobj.properties;

import lombok.Data;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "ai.agent.config",ignoreInvalidFields = true)
public class AiAgentAutoConfigPropertes {
    private boolean enabled=false;
    private Map<String, AIAgentConfigTableVO> tables;
}
