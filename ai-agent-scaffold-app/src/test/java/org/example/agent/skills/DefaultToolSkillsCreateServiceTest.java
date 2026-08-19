package org.example.agent.skills;

import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.skills.impl.DefaultToolSkillsCreateService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void buildsCallbacksFromJarBackedClasspathResource() throws IOException {
        Path jar = Files.createTempFile("skills", ".jar");
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader jarClassLoader = null;
        try {
            try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
                output.putNextEntry(new JarEntry("agent/"));
                output.closeEntry();
                output.putNextEntry(new JarEntry("agent/skills/"));
                output.closeEntry();
                output.putNextEntry(new JarEntry("agent/skills/jar-backed/"));
                output.closeEntry();
                output.putNextEntry(new JarEntry("agent/skills/jar-backed/SKILL.md"));
                output.write("""
                        ---
                        name: jar-backed
                        description: Skill loaded from a JAR
                        ---

                        Execute the JAR-backed skill.
                        """.getBytes(StandardCharsets.UTF_8));
                output.closeEntry();
            }

            jarClassLoader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, null);
            Thread.currentThread().setContextClassLoader(jarClassLoader);

            assertEquals("jar", new ClassPathResource("agent/skills").getURL().getProtocol());
            ToolCallback[] callbacks = assertDoesNotThrow(
                    () -> service.buildToolCallback(skills("resource", "agent/skills")));
            assertTrue(callbacks.length > 0);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            if (jarClassLoader != null) {
                jarClassLoader.close();
            }
            Files.deleteIfExists(jar);
        }
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
