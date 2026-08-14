package cn.bugstack.ai.test.domain.agent;

import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.Application;
import org.example.domain.agent.model.entity.ChatCommandEntity;
import org.example.domain.agent.service.IChatService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertFalse;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ChatServiceTest {
    @Resource
    private IChatService chatService;

    @Value("classpath:file/dog.png")
    private org.springframework.core.io.Resource imageResource;

    @Test
    public void test_handleMessage_01(){
        List<String>message = chatService.handleMessage("100003","xiaofuge","what can you do?");
        log.info("测试结果:{}", JSON.toJSONString(message));

    }
    @Test
    public void test_hanleMessage_04_withImage() throws IOException {
        String agentId = "100003";
        String userId = "xiaofuge";
        String sessionId = chatService.createSession(agentId, userId);
        ChatCommandEntity chatCommandEntity = ChatCommandEntity.builder()
                .agentId(agentId)
                .userId(userId)
                .sessionId(sessionId)
                .texts(List.of(new ChatCommandEntity.Content.Text("请识别这个图片告诉我这是什么动物")))
                .files(List.of())
                .inlineDatas(List.of(new ChatCommandEntity.Content.InlineData(imageResource.getContentAsByteArray(), MimeTypeUtils.IMAGE_JPEG_VALUE)))
                .build();
        List<String> message = chatService.handleMessage(chatCommandEntity);
        assertFalse(message.isEmpty());
        assertFalse(message.get(0).contains("Broken Image"));
        log.info("测试结果:{}",JSON.toJSONString(message));

    }
}



