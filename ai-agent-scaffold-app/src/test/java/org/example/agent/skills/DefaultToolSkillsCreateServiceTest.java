package org.example.agent.skills;

import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.skills.impl.DefaultToolSkillsCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultToolSkillsCreateServiceTest {
    private final DefaultToolSkillsCreateService service = new DefaultToolSkillsCreateService();

    @Test
    void buildsCallbacksFromClasspathResource() {
        ToolCallback[] callbacks = assertDoesNotThrow(() -> service.buildToolCallback(skills("resource", "agent/skills")));
        assertTrue(callbacks.length > 0);
    }

    @Test
    void rejectsBlankPath() {
        assertThrows(IllegalArgumentException.class, () -> service.buildToolCallback(skills("resource", "  ")));
    }

    @Test
    void rejectsUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> service.buildToolCallback(skills("classpath", "agent/skills")));
    }

    private AIAgentConfigTableVO.Module.ChatModel.ToolSkills skills(String type, String path) {
        AIAgentConfigTableVO.Module.ChatModel.ToolSkills skills = new AIAgentConfigTableVO.Module.ChatModel.ToolSkills();
        skills.setType(type);
        skills.setPath(path);
        return skills;
    }
}
