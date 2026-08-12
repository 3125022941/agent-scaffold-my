package org.example.domain.agent.service;

import org.example.domain.agent.model.valobj.AIAgentConfigTableVO;

import java.util.List;

public interface IArmoryService {
    void acceptArmoryAgents(List<AIAgentConfigTableVO> tables) throws Exception;
}
