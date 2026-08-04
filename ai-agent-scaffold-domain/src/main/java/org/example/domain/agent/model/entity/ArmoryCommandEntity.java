package org.example.domain.agent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArmoryCommandEntity {
    private AIAgentConfigTableVO aiAgentConfigTableVO;
}
