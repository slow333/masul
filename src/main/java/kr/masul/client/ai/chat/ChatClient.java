package kr.masul.client.ai.chat;

import kr.masul.client.ai.chat.dto.ChatRequest;
import kr.masul.client.ai.chat.dto.ChatResponse;

public interface ChatClient {

   ChatResponse generate(ChatRequest chatRequest);
}
