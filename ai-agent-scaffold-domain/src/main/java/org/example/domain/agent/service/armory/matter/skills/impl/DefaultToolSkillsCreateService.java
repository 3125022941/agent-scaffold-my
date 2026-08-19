package org.example.domain.agent.service.armory.matter.skills.impl;

import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.skills.ToolSkillsCreateService;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

public class DefaultToolSkillsCreateService implements ToolSkillsCreateService {
    @Override
    public ToolCallback[] buildToolCallback(AIAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills) throws Exception {

        String type = toolSkills.getType();
        String path = toolSkills.getPath();

        List<ToolCallback> toolCallbackList=new ArrayList<>();
        if("directory".equals(type)){
            ToolCallback toolCallback = SkillsTool.builder()
                    .addSkillsDirectory(path))
                    .build();
            toolCalllbackList.add(toolCallback);
        }
        if("resource".equals(type)){
            ToolCallback toolCallback = SkillsTool.builder()
                    .addSkillsResource(new ClassPathResource(path))
                    .build();
            toolCalllbackList.add(toolCallback);
        }

        return toolCallbackList.toArray(new ToolCallback[0]);
    }
}
