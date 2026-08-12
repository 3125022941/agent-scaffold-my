package org.example.domain.agent.model.valobj;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AIAgentConfigTableVO {
    // 整份 AI Agent 配置的外层容器
    private String appName;
    // Agent 的基础信息
    private Agent agent;
    private Module module;

    // Agent 基础字段
    @Data
    public static class Agent {
        private String agentId;
        private String agentName;
        private String agentDesc;

    }

    // 模块配置分组
    @Data
    public static class Module {
        private AiApi aiApi;
        private ChatModel chatModel;
        private List<Agent> agents;
        private List<AgentWorkflow> agentWorkflows;
        private Runner runner;

        // AI 接口相关配置
        @Data
        public static class AiApi {
            private String baseUrl;
            private String apiKey;
            private String completionsPath;
            private String embeddingsPath;
        }

        // 聊天模型相关配置
        @Data
        public static class ChatModel {
            private String model;
            private List<ToolMcp> toolMcpList;

            @Data
            public static class ToolMcp {
                private SSEServerParameters sse;
                private stdioServerParameters stdio;
            }

            // SSE 方式的服务参数
            @Data
            public static class SSEServerParameters {
                private String name;
                private String baseUri;
                private String sseEndpoint;
                private Integer requestTimeout = 3000;
            }

            // stdio 方式的服务参数
            @Data
            public static class stdioServerParameters {
                private String name;
                private Integer requestTimeout = 3000;
                private ServerParameters serverParameters;

                // 启动服务时的命令参数
                @Data
                public static class ServerParameters {
                    private String command;
                    private List<String> args;
                    private Map<String, String> env;
                }
            }
        }

        // 单个 Agent 的执行配置
        @Data
        public static class Agent {
            private String name;
            private String instruction;
            private String description;
            private String outputKey;
        }

        // 多个 Agent 的协作流程配置
        @Data
        public static class AgentWorkflow {
            private String type;
            private String name;
            private List<String> subAgents;
            private String description;
            private Integer maxIterations = 3;
        }

        @Data
        public static class Runner{
            private String agentName;
        }
    }
}
