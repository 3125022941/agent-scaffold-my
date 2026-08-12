package org.example.domain.agent.service.armory.matter.plugin;

import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.CallbackContext;
import com.google.adk.agents.Callbacks;
import com.google.adk.agents.InvocationContext;
import com.google.adk.models.LlmRequest;
import com.google.adk.models.LlmResponse;
import com.google.adk.plugins.BasePlugin;
import com.google.common.collect.ImmutableList;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Maybe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service("myTestPlugin")
public class MyTestPlugin extends BasePlugin {
    public MyTestPlugin(String name){
        super(name);
    }
    public MyTestPlugin(){
        super("MyTestPlugin");
    }

    @Override
    public Maybe<Content> onUserMessageCallback(InvocationContext invocationContext, Content userMessage) {
        log.info("用户输入信息:{}",userMessage.text());
        return super.onUserMessageCallback(invocationContext, userMessage);
    }

    @Override
    public Maybe<LlmResponse> beforeModelCallback(CallbackContext callbackContext, LlmRequest.Builder llmRequest) {
        Optional<String> model = llmRequest.build().model();
        log.info("ai 模型:{}",model.orElse(""));
        return super.beforeModelCallback(callbackContext, llmRequest);
    }

    @Override
    public Maybe<Content> beforeAgentCallback(BaseAgent agent, CallbackContext callbackContext) {
        log.info("智能体名称:{}", agent.name());

        return super.beforeAgentCallback(agent, callbackContext);
    }
}
