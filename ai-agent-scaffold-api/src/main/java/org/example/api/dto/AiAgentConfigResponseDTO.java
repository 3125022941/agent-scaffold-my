package org.example.api.dto;

import lombok.Data;

//智能体配置响应对象
@Data
public class AiAgentConfigResponseDTO {
    private String agentId;
    private String agentName;
    private String agentDesc;
}
