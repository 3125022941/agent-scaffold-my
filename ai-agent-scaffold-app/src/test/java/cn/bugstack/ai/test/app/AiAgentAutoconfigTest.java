package cn.bugstack.ai.test.app;

import com.alibaba.fastjson.JSON;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.Application;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class AiAgentAutoconfigTest {

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void test_agent() {
        AiAgentRegisterVO aiAgentRegisterVO = applicationContext.getBean("100001", AiAgentRegisterVO.class);

        assertEquals("testAgent", aiAgentRegisterVO.getAppName());
        assertEquals("100001", aiAgentRegisterVO.getAgentId());
        assertNotNull(aiAgentRegisterVO.getRunner());
    }
    @Test
    public void test_handlerMessage_03() {
        AiAgentRegisterVO aiAgentRegisterVO = applicationContext.getBean("100003", AiAgentRegisterVO.class);
        String appName = aiAgentRegisterVO.getAppName();
        InMemoryRunner runner=aiAgentRegisterVO.getRunner();
        Session session=runner.sessionService()
                .createSession(appName,"xiaofuge")
                .blockingGet();
        Content userMsg= Content.fromParts(Part.fromText("给我一份学习计划"));
        Flowable<Event>events=runner.runAsync("xiaofuge",session.id(),userMsg);

        List<String>outputs=new ArrayList<>();
        events.blockingForEach(event->outputs.add(event.stringifyContent()));
        log.info("测试结果:{}", JSON.toJSONString(outputs));

    }
}
