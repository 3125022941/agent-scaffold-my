package org.example.domain.agent.service.armory.matter.skills.impl;

import org.apache.commons.lang3.StringUtils;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.skills.ToolSkillsCreateService;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DefaultToolSkillsCreateService implements ToolSkillsCreateService {
    @Override
    public ToolCallback[] buildToolCallback(AIAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills) {
        if (toolSkills == null || StringUtils.isBlank(toolSkills.getPath())) {
            throw new IllegalArgumentException("Tool skills path must not be blank");
        }
        String type = StringUtils.trimToEmpty(toolSkills.getType());
        String path = toolSkills.getPath().trim();
        ToolCallback callback = switch (type) {
            case "directory" -> SkillsTool.builder().addSkillsDirectory(path).build();
            case "resource" -> SkillsTool.builder().addSkillsResource(new ClassPathResource(path)).build();
            default -> throw new IllegalArgumentException("Unsupported tool skills type: " + type);
        };
        return new ToolCallback[]{callback};
    }
}
