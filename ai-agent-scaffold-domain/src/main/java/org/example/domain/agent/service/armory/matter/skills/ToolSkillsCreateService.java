package org.example.domain.agent.service.armory.matter.skills;

import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.springframework.ai.tool.ToolCallback;

public interface ToolSkillsCreateService {
    ToolCallback[] buildToolCallback(AIAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills) throws Exception;

}
