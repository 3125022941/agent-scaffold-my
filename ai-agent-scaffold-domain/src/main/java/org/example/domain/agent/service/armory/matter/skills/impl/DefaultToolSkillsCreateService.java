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
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Enumeration;

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
        Path temporaryDirectory = null;
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

            temporaryDirectory = Files.createTempDirectory("agent-skills-");
            for (Resource skillResource : skillResources) {
                Path target = temporaryDirectory.resolve(relativeSkillPath(skillResource, normalizedPath, classLoader)).normalize();
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
            cleanupMaterializedSkills(temporaryDirectory, ex);
            throw new IllegalStateException("Failed to materialize tool skills resource path: " + resourcePath, ex);
        } catch (RuntimeException ex) {
            cleanupMaterializedSkills(temporaryDirectory, ex);
            throw ex;
        }
    }

    private Path relativeSkillPath(Resource skillResource, String resourcePath, ClassLoader classLoader) throws IOException {
        URL skillUrl = skillResource.getURL();
        if ("jar".equals(skillUrl.getProtocol())) {
            String resourceEntryPath = skillUrl.toExternalForm();
            int separatorIndex = resourceEntryPath.lastIndexOf("!/");
            if (separatorIndex < 0) {
                throw new IllegalStateException("Invalid JAR skill resource URL: " + skillUrl);
            }
            return Path.of(removeResourceRoot(
                    decodeUriPath(resourceEntryPath.substring(separatorIndex + 2)), resourcePath, skillResource));
        }
        if ("file".equals(skillUrl.getProtocol())) {
            return relativeFileSkillPath(skillResource, resourcePath, classLoader);
        }
        throw new IllegalStateException("Unsupported skill resource URL protocol: " + skillUrl.getProtocol());
    }

    private Path relativeFileSkillPath(Resource skillResource, String resourcePath, ClassLoader classLoader) throws IOException {
        Path skillPath = Paths.get(skillResource.getURI());
        Enumeration<URL> resourceRoots = classLoader.getResources(resourcePath);
        while (resourceRoots.hasMoreElements()) {
            URL resourceRoot = resourceRoots.nextElement();
            if ("file".equals(resourceRoot.getProtocol())) {
                Path rootPath = Paths.get(URI.create(resourceRoot.toExternalForm()));
                if (skillPath.startsWith(rootPath)) {
                    return rootPath.relativize(skillPath);
                }
            }
        }
        throw new IllegalStateException("Skill resource is outside configured path: " + skillResource);
    }

    private String removeResourceRoot(String resourceEntryPath, String resourcePath, Resource skillResource) {
        String resourceRoot = resourcePath + "/";
        if (!resourceEntryPath.startsWith(resourceRoot)) {
            throw new IllegalStateException("Skill resource is outside configured path: " + skillResource);
        }
        return resourceEntryPath.substring(resourceRoot.length());
    }

    private String decodeUriPath(String path) {
        return URI.create("file:/" + path).getPath().substring(1);
    }

    private void cleanupMaterializedSkills(Path directory, Exception failure) {
        if (directory == null) {
            return;
        }
        try {
            deleteRecursively(directory);
        } catch (IOException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private void registerForDeletionOnExit(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.comparingInt(Path::getNameCount))
                    .forEach(path -> path.toFile().deleteOnExit());
        }
    }

    private void deleteRecursively(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private String trimSlashes(String path) {
        return path.replaceAll("^/+|/+$", "");
    }
}
