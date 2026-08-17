package org.example.api;

import org.example.api.dto.AiAgentConfigResponseDTO;
import org.example.api.dto.ChatRequestDTO;
import org.example.api.dto.ChatResponseDTO;
import org.example.api.dto.CreateSessionRequestDTO;
import org.example.api.dto.CreateSessionResponseDTO;
import org.example.api.response.Response;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.List;

//智能体服务接口
public interface IAgentService {
    Response<List<AiAgentConfigResponseDTO>> queryAiAgentConfigList();

    Response<CreateSessionResponseDTO> createSession(CreateSessionRequestDTO requestDTO);

    Response<ChatResponseDTO> chat(ChatRequestDTO requestDTO);

    ResponseBodyEmitter chatStream(ChatRequestDTO requestDTO);

}
