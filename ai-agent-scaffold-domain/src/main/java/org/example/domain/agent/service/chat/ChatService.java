package org.example.domain.agent.service.chat;

import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.agent.model.entity.ChatCommandEntity;
import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.model.valobj.properties.AiAgentAutoConfigPropertes;
import org.example.domain.agent.service.IChatService;
import org.example.domain.agent.service.armory.factory.DefaultArmoryFactory;
import org.example.types.enums.ResponseCode;
import org.example.types.exception.AppException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ChatService implements IChatService { //chat服务承诺实现ichat服务规定的所有方法

    @Resource
    private DefaultArmoryFactory defaultArmoryFactory;

    @Resource
    private AiAgentAutoConfigPropertes aiAgentAutoConfigPropertes;

    private final Map<String, String> userSession = new ConcurrentHashMap<>();

    @Override
    public List<AIAgentConfigTableVO.Agent> queryAiAgentConfig() {
        Map<String, AIAgentConfigTableVO> tables = aiAgentAutoConfigPropertes.getTables(); //配置表集合
        List<AIAgentConfigTableVO.Agent> agentList = new ArrayList<>();//空列表
        if (tables != null) {
            for (AIAgentConfigTableVO vo : tables.values()) { //从 tables.values() 的所有配置中，每次取一份，叫作 vo。
                if (null!=vo.getAgent()) {
                    agentList.add(vo.getAgent());
                }
            }
        }
        return agentList;
    }

    @Override
    public String createSession(String agentId, String userId) {
        AiAgentRegisterVO aiAgentRegisterVO=defaultArmoryFactory.getAiAgentRegisterVO(agentId);
        if(null==aiAgentRegisterVO){
            throw new AppException(ResponseCode.E0001.getCode());
        }

        String appName = aiAgentRegisterVO.getAppName();
        InMemoryRunner runner=aiAgentRegisterVO.getRunner();

        return userSession.computeIfAbsent(userId, uid->{
            Session session=runner.sessionService().createSession(appName,uid)
                    .blockingGet();
            return session.id();
        });
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String message) {

        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);
        if(null==aiAgentRegisterVO){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        String sessionId = createSession(agentId, userId);

        return handleMessage(agentId, userId, sessionId, message);
    }

    @Override
    public List<String> handleMessage(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);
        if(null==aiAgentRegisterVO){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        InMemoryRunner runner=aiAgentRegisterVO.getRunner();

        Content userMsg = Content.fromParts(Part.fromText(message));
        Flowable<Event> events = runner.runAsync(userId, sessionId, userMsg);
        List<String>outputs=new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));
        return outputs;
    }

    @Override
    public Flowable<Event> handleMessageStream(String agentId, String userId, String sessionId, String message) {
        AiAgentRegisterVO aiAgentRegisterVO = defaultArmoryFactory.getAiAgentRegisterVO(agentId);
        if(null==aiAgentRegisterVO){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        InMemoryRunner runner=aiAgentRegisterVO.getRunner();

        Content userMsg = Content.fromParts(Part.fromText(message));
        return runner.runAsync(userId, sessionId, userMsg);


    }

    @Override
    public List<String> handleMessage(ChatCommandEntity chatCommandEntity) {
        AiAgentRegisterVO aiAgentRegisterVO=defaultArmoryFactory.getAiAgentRegisterVO(chatCommandEntity.getAgentId());
        if(null==aiAgentRegisterVO){
            throw new AppException(ResponseCode.E0001.getCode());
        }
        List<Part>parts=new ArrayList<>();
        List<ChatCommandEntity.Content.Text>texts=chatCommandEntity.getTexts();
        if(null!=texts && !texts.isEmpty()){
            for(ChatCommandEntity.Content.Text text:texts){
                parts.add(Part.fromText(text.getMessage()));
            }
        }
        List<ChatCommandEntity.Content.File>files=chatCommandEntity.getFiles();
        if(null!=files && !files.isEmpty()){
            for(ChatCommandEntity.Content.File file:files){
                parts.add(Part.fromUri(file.getFileUri(),file.getMimeType()));
            }
        }
        List<ChatCommandEntity.Content.InlineData>inlineDatas=chatCommandEntity.getInlineDatas();
        if(null!=inlineDatas&&!inlineDatas.isEmpty()){
            for(ChatCommandEntity.Content.InlineData inlineData:inlineDatas){
                parts.add(Part.fromBytes(inlineData.getBytes(),inlineData.getMimeType()));
            }
        }
        Content content = Content.builder().role("user").parts(parts).build();
        InMemoryRunner runner = aiAgentRegisterVO.getRunner();
        Flowable<Event>events=runner.runAsync(chatCommandEntity.getUserId(),chatCommandEntity.getSessionId(),content);

        List<String>outputs=new ArrayList<>();
        events.blockingForEach(event -> outputs.add(event.stringifyContent()));
        return outputs;
    }

}
