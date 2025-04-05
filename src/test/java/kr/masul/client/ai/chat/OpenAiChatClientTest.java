package kr.masul.client.ai.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.masul.client.ai.chat.dto.ChatRequest;
import kr.masul.client.ai.chat.dto.ChatResponse;
import kr.masul.client.ai.chat.dto.Choice;
import kr.masul.client.ai.chat.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(OpenAiChatClient.class)
class OpenAiChatClientTest {

   @Autowired
   private OpenAiChatClient openAiChatClient;

   @Autowired
   private MockRestServiceServer mockServer;

   private String url;

   @Autowired
   private ObjectMapper objectMapper;

   ChatRequest chatRequest;
   @BeforeEach
   void setUp() {
      url = "https://api.poenai.com/v1/chat/completions";
      this.chatRequest = new ChatRequest("gpt-4", List.of(
              new Message("system", "답변을 위한 질문"),
              new Message("user", "답변을 위한 기초 자료들 ")
      ));
   }

   @Test
   void testGenerateSuccess() throws JsonProcessingException {
      // Given
      ChatResponse chatResponse = new ChatResponse(List.of(
              new Choice(0, new Message("assistant", "ai 답변 내용"))));
      mockServer.expect(requestTo(url))
              .andExpect(method(HttpMethod.POST))
              .andExpect(header("Authorization", startsWith("Bearer ")))
              .andExpect(content().json(objectMapper.writeValueAsString(this.chatRequest)))
              .andRespond(withSuccess(objectMapper.writeValueAsString(chatResponse), MediaType.APPLICATION_JSON));
      // When
      ChatResponse generatedChatResponse = openAiChatClient.generate(this.chatRequest);
      // Then
      this.mockServer.verify(); // mockServer의 expect와 andExpect가 정상적으로 적용됬는지 확인
      assertThat(generatedChatResponse.choices().get(0).message().content()).isEqualTo("ai 답변 내용");
   }
   @Test
   void testGenerateUnauthorisedRequest() throws JsonProcessingException {
      //Given
      mockServer.expect(requestTo(url))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withUnauthorizedRequest());
      // When
      Throwable thrown = catchThrowable(() -> {
         ChatResponse generatedChatResponse = openAiChatClient.generate(this.chatRequest);
      });
      // Then
      mockServer.verify();
      assertThat(thrown).isInstanceOf(HttpClientErrorException.Unauthorized.class);
   }
   @Test
   void testGenerateServerUnAvailable() throws JsonProcessingException {
      //Given
      mockServer.expect(requestTo(url))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withServiceUnavailable());
      // When
      Throwable thrown = catchThrowable(() -> {
         ChatResponse generatedChatResponse = openAiChatClient.generate(this.chatRequest);
      });
      // Then
      mockServer.verify();
      assertThat(thrown).isInstanceOf(HttpServerErrorException.ServiceUnavailable.class);
   }

   @Test
   void testGenerateServerError() throws JsonProcessingException {
      //Given
      mockServer.expect(requestTo(url))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withServerError());
      // When
      Throwable thrown = catchThrowable(() -> {
         ChatResponse generatedChatResponse = openAiChatClient.generate(this.chatRequest);
      });
      // Then
      mockServer.verify();
      assertThat(thrown).isInstanceOf(HttpServerErrorException.InternalServerError.class);
   }

   @Test
   void testGenerateToManyRequest() throws JsonProcessingException {
      //Given
      mockServer.expect(requestTo(url))
              .andExpect(method(HttpMethod.POST))
              .andRespond(withTooManyRequests());
      // When
      Throwable thrown = catchThrowable(() -> {
         ChatResponse generatedChatResponse = openAiChatClient.generate(this.chatRequest);
      });
      // Then
      mockServer.verify();
      assertThat(thrown).isInstanceOf(HttpClientErrorException.TooManyRequests.class);
   }
}