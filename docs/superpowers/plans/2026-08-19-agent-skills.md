# Agent Skills 装配 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 从 Agent YAML 装配 classpath 中的 `battle-plan` Skills 工具回调，并与 MCP 工具一起注入 ChatModel。

**Architecture:** `ToolSkillsCreateService` 封装 `SkillsTool` 构建细节，按 `resource` 或 `directory` 返回 `ToolCallback[]`。`ChatModelNode` 仅合并 MCP 与 Skills 的回调列表。

**Tech Stack:** Java 17, Spring Boot 3.4.3, Spring AI 1.1.5, `spring-ai-agent-utils` 0.4.2, JUnit 5。

## Global Constraints

- `type` 仅允许 `resource` 和 `directory`；空值或其他值抛出 `IllegalArgumentException`。
- `path` 不可为 null、空串或空白。
- 内置示例只增加 `agent/skills/battle-plan/SKILL.md`；YAML 使用 `resource: agent/skills`。
- Skills 构建失败必须中断 Agent 装配；MCP 的现有容错行为不变。

---

### Task 1: 写入 Skills 工厂失败测试与示例资源

**Files:**
- Create: `ai-agent-scaffold-app/src/test/java/org/example/agent/skills/DefaultToolSkillsCreateServiceTest.java`
- Create: `ai-agent-scaffold-app/src/main/resources/agent/skills/battle-plan/SKILL.md`

**Interfaces:** Consumes `ToolSkillsCreateService#buildToolCallback(ToolSkills)` and produces a non-empty `ToolCallback[]` for a valid resource, otherwise `IllegalArgumentException`.

- [ ] **Step 1: Write the failing test**

```java
class DefaultToolSkillsCreateServiceTest {
    private final DefaultToolSkillsCreateService service = new DefaultToolSkillsCreateService();

    @Test void buildsCallbacksFromClasspathResource() {
        ToolCallback[] callbacks = assertDoesNotThrow(() -> service.buildToolCallback(skills("resource", "agent/skills")));
        assertTrue(callbacks.length > 0);
    }

    @Test void rejectsBlankPath() {
        assertThrows(IllegalArgumentException.class, () -> service.buildToolCallback(skills("resource", "  ")));
    }

    @Test void rejectsUnsupportedType() {
        assertThrows(IllegalArgumentException.class, () -> service.buildToolCallback(skills("classpath", "agent/skills")));
    }

    private AIAgentConfigTableVO.Module.ChatModel.ToolSkills skills(String type, String path) {
        AIAgentConfigTableVO.Module.ChatModel.ToolSkills skills = new AIAgentConfigTableVO.Module.ChatModel.ToolSkills();
        skills.setType(type); skills.setPath(path); return skills;
    }
}
```

Use imports for `AIAgentConfigTableVO`, `DefaultToolSkillsCreateService`, JUnit Jupiter, `ToolCallback`, `assertDoesNotThrow`, `assertThrows`, and `assertTrue`.

- [ ] **Step 2: Verify the expected red state**

Run `mvn -pl ai-agent-scaffold-app -am -Dtest=DefaultToolSkillsCreateServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`.

Expected: Domain compilation fails due to the baseline invalid parenthesis and misspelled `toolCalllbackList` in `DefaultToolSkillsCreateService`.

- [ ] **Step 3: Add the classpath resource**

Write `SKILL.md` with this content:

```markdown
---
name: battle-plan
description: 分析计算机性能问题并提供安全的优化计划。
---

# Battle Plan

1. 询问操作系统、硬件配置和可复现现象。
2. 按 CPU、内存、磁盘、启动项和网络识别瓶颈。
3. 先提供不删除用户数据的低风险建议，说明每项收益和验证方法。
4. 不执行破坏性命令，也不要添写系统设置。
```

- [ ] **Step 4: Commit**

Run `git add ai-agent-scaffold-app/src/test/java/org/example/agent/skills/DefaultToolSkillsCreateServiceTest.java ai-agent-scaffold-app/src/main/resources/agent/skills/battle-plan/SKILL.md && git commit -m "test: cover skills callback configuration"`.

### Task 2: Repair and register the Skills factory

**Files:**
- Modify: `ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/service/armory/matter/skills/impl/DefaultToolSkillsCreateService.java`

**Interfaces:** Produces `ToolCallback[] buildToolCallback(ToolSkills toolSkills)` and is injectable as a Spring `@Service`.

- [ ] **Step 1: Replace the factory implementation**

```java
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
```

Add imports for `StringUtils` and `Service`; remove collection imports. Retain `SkillsTool`, `ClassPathResource`, and `ToolCallback` imports.

- [ ] **Step 2: Verify green**

Run `mvn -pl ai-agent-scaffold-app -am -Dtest=DefaultToolSkillsCreateServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`.

Expected: `Tests run: 3, Failures: 0, Errors: 0`.

- [ ] **Step 3: Check the third-party API signature**

Run `javap -classpath ~/.m2/repository/org/springaicommunity/spring-ai-agent-utils/0.4.2/spring-ai-agent-utils-0.4.2.jar 'org.springaicommunity.agent.tools.SkillsTool$Builder'`.

Expected: output includes `addSkillsDirectory(java.lang.String)` and `addSkillsResource(org.springframework.core.io.Resource)`.

- [ ] **Step 4: Commit**

Run `git add ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/service/armory/matter/skills/impl/DefaultToolSkillsCreateService.java && git commit -m "feat: build skills tool callbacks from agent config"`.

### Task 3: Complete ChatModel wiring and YAML example

**Files:**
- Modify: `ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/model/valobj/AIAgentConfigTableVO.java`
- Modify: `ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/service/armory/node/ChatModelNode.java`
- Modify: `ai-agent-scaffold-app/src/main/resources/agent/only-one-agent.yml`

**Interfaces:** Consumes `chat-model.tool-skills-list` as `List<ToolSkills>` and produces one `OpenAiChatOptions` callback list combining MCP and Skills.

- [ ] **Step 1: Normalize the config type formatting**

```java
private List<ToolMcp> toolMcpList;
private List<ToolSkills> toolSkillsList;

@Data
public static class ToolSkills {
    private String type = "directory";
    private String path;
}
```

- [ ] **Step 2: Keep Skills callback assembly explicit and strict**

```java
if (toolSkillsList != null) {
    for (AIAgentConfigTableVO.Module.ChatModel.ToolSkills toolSkills : toolSkillsList) {
        ToolCallback[] toolCallbacks = toolSkillsCreateService.buildToolCallback(toolSkills);
        toolCallbackList.addAll(List.of(toolCallbacks));
    }
}
```

Do not add a `try/catch` around this loop. Keep the existing MCP `try/catch` unchanged.

- [ ] **Step 3: Correct the local YAML key and preserve the resource configuration**

```yaml
- local:
    name: myToolCallbackProvider
tool-skills-list:
  - type: resource
    path: agent/skills
```

Do not alter existing API key or MCP endpoint values.

- [ ] **Step 4: Build and run the application**

Run `mvn -pl ai-agent-scaffold-app -am package -DskipTests` and then `timeout 45s java -jar ai-agent-scaffold-app/target/ai-agent-scaffold-app.jar --spring.profiles.active=dev`.

Expected: package reports `BUILD SUCCESS`; startup logs include `Ai Agent 装配工作 - ChatModelNode` and no Skills factory Bean injection exception. External MCP/model credential failure is an environment limitation and must not be fixed in this task.

- [ ] **Step 5: Commit**

Run `git add ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/model/valobj/AIAgentConfigTableVO.java ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/service/armory/node/ChatModelNode.java ai-agent-scaffold-app/src/main/resources/agent/only-one-agent.yml && git commit -m "feat: attach configured skills to chat models"`.

### Task 4: Run regression verification

**Files:**
- Verify: `ai-agent-scaffold-domain/src/main/java/org/example/domain/agent/service/armory/matter/skills/impl/DefaultToolSkillsCreateService.java`
- Verify: `ai-agent-scaffold-app/src/main/resources/agent/skills/battle-plan/SKILL.md`
- Verify: `ai-agent-scaffold-app/src/main/resources/agent/only-one-agent.yml`

**Interfaces:** Consumes the completed Skills factory, YAML configuration, and classpath resource; produces reproducible test and build evidence.

- [ ] **Step 1: Run targeted tests**

Run `mvn -pl ai-agent-scaffold-app -am -Dtest=DefaultToolSkillsCreateServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`.

Expected: `Tests run: 3, Failures: 0, Errors: 0`.

- [ ] **Step 2: Run complete test and package commands**

Run `mvn test && mvn package -DskipTests`.

Expected: both commands exit `0` and report `BUILD SUCCESS`.

- [ ] **Step 3: Check the worktree**

Run `git diff --check && git status --short`.

Expected: no whitespace errors; no unplanned edits beyond pre-existing user changes.

- [ ] **Step 4: Commit documentation**

Run `git add docs/superpowers/specs/2026-08-19-agent-skills-design.md docs/superpowers/plans/2026-08-19-agent-skills.md && git commit -m "docs: document agent skills assembly"`.
