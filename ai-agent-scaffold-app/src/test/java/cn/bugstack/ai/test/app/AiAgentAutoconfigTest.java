package cn.bugstack.ai.test.app;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.Application;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.example.domain.agent.service.IChatService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
        IChatService chatService = applicationContext.getBean(IChatService.class);
        String sessionId = chatService.createSession("100003", "xiaofuge");
        List<String> outputs = chatService.handleMessage("100003", "xiaofuge", sessionId,
                "把xiaofuge转换为大写");

        assertFalse(outputs.isEmpty());
        log.info("测试结果:{}", JSON.toJSONString(outputs));

    }
}
