# Agent Skills 装配设计

## 目标

为 AI Agent 的 `chat-model` 增加 Skills 工具回调装配能力。Skills 配置与现有 MCP 配置并存，统一装配给 `OpenAiChatOptions`。

本次仅提供一个工程内置示例技能书：`battle-plan`。

## 配置模型

`AIAgentConfigTableVO.Module.ChatModel` 保持以下字段：

```java
private List<ToolMcp> toolMcpList;
private List<ToolSkills> toolSkillsList;
```

`ToolSkills` 提供两个字段：

- `type`：`resource` 或 `directory`。
- `path`：`resource` 时为 classpath 相对路径；`directory` 时为本地绝对路径。

不支持的类型与空路径必须抛出包含具体配置项的 `IllegalArgumentException`，防止 Agent 在缺失必需技能时静默启动。

## 装配边界

`DefaultToolSkillsCreateService` 实现 `ToolSkillsCreateService` 并注册为 Spring `@Service`。它是唯一与 `SkillsTool` 直接交互的类：

- `directory`：调用 `SkillsTool.builder().addSkillsDirectory(path).build()`。
- `resource`：调用 `SkillsTool.builder().addSkillsResource(new ClassPathResource(path)).build()`。

`ChatModelNode` 仅负责遍历 `toolSkillsList`，将所有返回的 `ToolCallback` 加入现有 MCP 回调列表，再创建 ChatModel。MCP 可用性处理保持现有行为；Skills 配置错误需上抛，以确保配置可信。

## 资源与 YAML

新增 `ai-agent-scaffold-app/src/main/resources/agent/skills/battle-plan/SKILL.md`。该技能书仅定义计算机性能诊断与优化建议的工作方式，不包含任何依赖于平台的脚本。

`only-one-agent.yml` 使用以下配置：

```yaml
tool-skills-list:
  - type: resource
    path: agent/skills
```

## 验证

1. 工厂测试在 classpath 中装配 `agent/skills` 返回非空回调。
2. 工厂测试覆盖空路径和不支持类型的明确失败。
3. 运行 Domain 模块测试、全工程编译与应用启动验证，确认 YAML 绑定后能完成 Agent 装配。
