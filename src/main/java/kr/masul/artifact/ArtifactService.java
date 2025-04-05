package kr.masul.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.annotation.Timed;
import jakarta.transaction.Transactional;
import kr.masul.client.ai.chat.ChatClient;
import kr.masul.client.ai.chat.dto.ChatRequest;
import kr.masul.client.ai.chat.dto.ChatResponse;
import kr.masul.client.ai.chat.dto.Message;
import kr.masul.system.IdWorker;
import kr.masul.system.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtifactService {

   private final ArtifactRepository artifactRepository;
   private final IdWorker idWorker;
   private final ChatClient chatClient;

   public Artifact findById(String artifactId) {
      return artifactRepository
              .findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact",artifactId));

   }

   @Timed("동작 시간 측정용") // 동작하지 않음
   public List<Artifact> findAll() {
      return artifactRepository.findAll();
   }

   public Artifact add(Artifact artifact) {
      artifact.setId(idWorker.nextId() + "");
      return artifactRepository.save(artifact);
   }

   public Artifact update(String artifactId, Artifact update) {
      Artifact oldArtifact = artifactRepository.findById(artifactId)
              .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
      oldArtifact.setId(update.getId());
      oldArtifact.setName(update.getName());
      oldArtifact.setDescription(update.getDescription());
      oldArtifact.setImageUrl(update.getImageUrl());
      oldArtifact.setOwner(update.getOwner());
      artifactRepository.save(oldArtifact);
      return oldArtifact;
   }

   public void delete(String artifactId) {
      Artifact artifact = artifactRepository.findById(artifactId)
              .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));
      artifactRepository.deleteById(artifactId);
   }
   public String summarize(List<ArtifactDto> artifactDtos) throws JsonProcessingException {
      // LocalDateTime이 있으면 pom.xml에 dependency 추가하고 regitsterModule() 추가해야함
      ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
      String json = objectMapper.writeValueAsString(artifactDtos);

      ChatRequest chatRequest = new ChatRequest("gpt-4", List.of(
              new Message("system", "질문할 내용"),
              new Message("user", json)
      ));

      ChatResponse generatedResponse = chatClient.generate(chatRequest);
      return generatedResponse.choices().get(0).message().content();
   }
}
