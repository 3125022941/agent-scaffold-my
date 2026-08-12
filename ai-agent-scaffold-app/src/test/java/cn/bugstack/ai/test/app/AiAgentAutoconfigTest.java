package cn.bugstack.ai.test.app;

import jakarta.annotation.Resource;
import org.example.Application;
import org.example.domain.agent.model.valobj.AiAgentRegisterVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
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
}
