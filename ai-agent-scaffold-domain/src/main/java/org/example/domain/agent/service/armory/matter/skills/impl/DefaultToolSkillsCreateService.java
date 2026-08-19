package org.example.domain.agent.service.armory.matter.skills.impl;

import org.apache.commons.lang3.StringUtils;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.service.armory.matter.skills.ToolSkillsCreateService;
import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

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
            case "resource" -> SkillsTool.builder().addSkillsDirectory(materializeSkillsResource(path)).build();
            default -> throw new IllegalArgumentException("Unsupported tool skills type: " + type);
        };
        return new ToolCallback[]{callback};
    }

    private String materializeSkillsResource(String resourcePath) {
        String normalizedPath = trimSlashes(resourcePath);
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = DefaultToolSkillsCreateService.class.getClassLoader();
            }
            Resource[] skillResources = new PathMatchingResourcePatternResolver(classLoader)
                    .getResources("classpath*:" + normalizedPath + "/**/SKILL.md");
            if (skillResources.length == 0) {
                throw new IllegalArgumentException("No SKILL.md files found for tool skills resource path: " + resourcePath);
            }

            Path temporaryDirectory = Files.createTempDirectory("agent-skills-");
            for (Resource skillResource : skillResources) {
                Path target = temporaryDirectory.resolve(relativeSkillPath(skillResource, normalizedPath)).normalize();
                if (!target.startsWith(temporaryDirectory)) {
                    throw new IllegalStateException("Invalid skill resource path: " + skillResource);
                }
                Files.createDirectories(target.getParent());
                try (InputStream inputStream = skillResource.getInputStream()) {
                    Files.copy(inputStream, target);
                }
            }
            registerForDeletionOnExit(temporaryDirectory);
            return temporaryDirectory.toString();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to materialize tool skills resource path: " + resourcePath, ex);
        }
    }

    private String relativeSkillPath(Resource skillResource, String resourcePath) throws IOException {
        String decodedPath = URLDecoder.decode(skillResource.getURL().getPath(), StandardCharsets.UTF_8);
        String marker = resourcePath + "/";
        int markerIndex = decodedPath.lastIndexOf(marker);
        if (markerIndex < 0) {
            throw new IllegalStateException("Skill resource is outside configured path: " + skillResource);
        }
        return decodedPath.substring(markerIndex + marker.length());
    }

    private void registerForDeletionOnExit(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.comparingInt(Path::getNameCount))
                    .forEach(path -> path.toFile().deleteOnExit());
        }
    }

    private String trimSlashes(String path) {
        return path.replaceAll("^/+|/+$", "");
    }
}
