package org.example.domain.agent.model.valobj;

import com.google.adk.runner.InMemoryRunner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentRegisterVO {
    private String appName; //智能体名称
    private String agentId;
    private String agentName;
    private String agentDesc;//描述
    private InMemoryRunner runner;//智能体执行对象
}
