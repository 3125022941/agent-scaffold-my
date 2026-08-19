package org.example.trigger.http;

import com.google.adk.events.Event;
import io.reactivex.rxjava3.core.Flowable;
import org.example.api.dto.ChatRequestDTO;
import org.example.api.dto.ChatResponseDTO;
import org.example.api.dto.CreateSessionRequestDTO;
import org.example.api.dto.CreateSessionResponseDTO;
import org.example.api.response.Response;
import org.example.domain.agent.service.IChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AgentServiceControllerTest {

    @Mock
    private IChatService chatService;

    @InjectMocks
    private AgentServiceController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createSessionReturnsTheServiceSessionId() {
        CreateSessionRequestDTO request = new CreateSessionRequestDTO();
        request.setAgentId("test-agent");
        request.setUserId("user-1");
        when(chatService.createSession("test-agent", "user-1")).thenReturn("session-1");

        Response<CreateSessionResponseDTO> response = controller.createSession(request);

        assertEquals("0000", response.getCode());
        assertEquals("session-1", response.getData().getSessionId());
        verify(chatService).createSession("test-agent", "user-1");
    }

    @Test
    void createSessionAcceptsPostJson() throws Exception {
        when(chatService.createSession("test-agent", "user-1")).thenReturn("session-1");

        mockMvc.perform(post("/api/v1/create_session")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agentId\":\"test-agent\",\"userId\":\"user-1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.sessionId").value("session-1"));

        verify(chatService).createSession("test-agent", "user-1");
    }

    @Test
    void createSessionAcceptsGetQueryParameters() throws Exception {
        when(chatService.createSession("test-agent", "user-1")).thenReturn("session-1");

        mockMvc.perform(get("/api/v1/create_session")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("agentId", "test-agent")
                        .queryParam("userId", "user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.sessionId").value("session-1"));

        verify(chatService).createSession("test-agent", "user-1");
    }

    @Test
    void chatCreatesASessionWhenTheRequestHasNone() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setAgentId("test-agent");
        request.setUserId("user-1");
        request.setMessage("hello");
        when(chatService.createSession("test-agent", "user-1")).thenReturn("session-1");
        when(chatService.handleMessage("test-agent", "user-1", "session-1", "hello"))
                .thenReturn(List.of("first reply", "second reply"));

        Response<ChatResponseDTO> response = controller.chat(request);

        assertEquals("0000", response.getCode());
        assertEquals("first reply\nsecond reply", response.getData().getContent());
        verify(chatService).createSession("test-agent", "user-1");
        verify(chatService).handleMessage("test-agent", "user-1", "session-1", "hello");
    }

    @Test
    void chatUsesTheExistingSession() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setAgentId("test-agent");
        request.setUserId("user-1");
        request.setSessionId("session-1");
        request.setMessage("hello");
        when(chatService.handleMessage("test-agent", "user-1", "session-1", "hello"))
                .thenReturn(List.of("reply"));

        Response<ChatResponseDTO> response = controller.chat(request);

        assertEquals("reply", response.getData().getContent());
        verify(chatService, never()).createSession("test-agent", "user-1");
        verify(chatService).handleMessage("test-agent", "user-1", "session-1", "hello");
    }

    @Test
    void chatStreamReturnsAnEmitterAndSubscribesToTheServiceStream() {
        ChatRequestDTO request = new ChatRequestDTO();
        request.setAgentId("test-agent");
        request.setUserId("user-1");
        request.setSessionId("session-1");
        request.setMessage("hello");
        Event event = mock(Event.class);
        when(event.stringifyContent()).thenReturn("streaming reply");
        when(chatService.handleMessageStream("test-agent", "user-1", "session-1", "hello"))
                .thenReturn(Flowable.just(event));

        ResponseBodyEmitter emitter = controller.chatStream(request);

        assertNotNull(emitter);
        verify(chatService).handleMessageStream("test-agent", "user-1", "session-1", "hello");
        verify(event).stringifyContent();
    }
}
