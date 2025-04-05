package kr.masul.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.masul.client.ai.chat.ChatClient;
import kr.masul.client.ai.chat.dto.ChatRequest;
import kr.masul.client.ai.chat.dto.ChatResponse;
import kr.masul.client.ai.chat.dto.Choice;
import kr.masul.client.ai.chat.dto.Message;
import kr.masul.system.IdWorker;
//import kr.masul.system.ModuleConfig;
import kr.masul.system.exception.ObjectNotFoundException;
import kr.masul.wizard.WizardDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class ArtifactServiceTest {

   @Mock
   ArtifactRepository artifactRepository;
   @Mock
   IdWorker idWorker;
   @Mock
   ChatClient chatClient;

   @InjectMocks
   ArtifactService artifactService;

   @BeforeEach
   void setUp() {
   }

   @Test
   @DisplayName("find artifact by id success")
   void testFindByIdSuccess() {
      // Given
      Artifact a = new Artifact();
      a.setId("12303");
      a.setName("Third Artifact");
      a.setDescription("Third Artifact get large");
      a.setImageUrl("image");
      a.setOwner(null);

      given(artifactRepository.findById("12303")).willReturn(Optional.of(a));
      // When
      Artifact foundArtifact = artifactService.findById("12303");
      // then
      assertThat(foundArtifact.getId()).isEqualTo("12303");
      assertThat(foundArtifact.getName()).isEqualTo("Third Artifact");
      assertThat(foundArtifact.getDescription()).isEqualTo("Third Artifact get large");
      verify(artifactRepository, times(1)).findById("12303");
   }
   @Test
   @DisplayName("find artifact by id fail")
   void testFindByIdNotFound() {
      // Given
      Artifact a = new Artifact();
      a.setId("12303");
      a.setName("Third Artifact");
      a.setDescription("Third Artifact get large");
      a.setImageUrl("image");
      a.setOwner(null);

      given(artifactRepository.findById(Mockito.anyString())).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         Artifact foundArtifact = artifactService.findById("12303");
      });
      // then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
              .hasMessage("Could not find artifact with id 12303");
      verify(artifactRepository, times(1)).findById("12303");
   }

   @Test
   void testFindAllSuccess() {
      // Given
      List<Artifact> artifactList = new ArrayList<>();
      Artifact a1 = new Artifact();
      a1.setId("12301");
      a1.setName("First Artifact");
      a1.setDescription("First Artifact hide");
      a1.setImageUrl("image");
      artifactList.add(a1);

      Artifact a2 = new Artifact();
      a2.setId("12302");
      a2.setName("Second Artifact");
      a2.setDescription("Second Artifact get small");
      a2.setImageUrl("image");
      artifactList.add(a2);

      given(artifactRepository.findAll()).willReturn(artifactList);
      // When
      List<Artifact> artifactAll = artifactService.findAll();
      // Then
      assertThat(artifactAll.size()).isEqualTo(2);
      verify(artifactRepository, times(1)).findAll();
   }

   @Test
   void testAddSuccess() {
      // Given
      Artifact a = new Artifact();
      a.setId("12311");
      a.setName("Second Artifact");
      a.setDescription("Second Artifact get small");
      a.setImageUrl("image");
      a.setOwner(null);

      given(idWorker.nextId()).willReturn(12311L);
      given(artifactRepository.save(a)).willReturn(a);
      // When
      Artifact addedArtifact = artifactService.add(a);
      // Then
      assertThat(addedArtifact.getId()).isEqualTo("12311");
      assertThat(addedArtifact.getDescription()).isEqualTo("Second Artifact get small");
      verify(artifactRepository, times(1)).save(a);
   }

   @Test
   void testUpdateSuccess() {
      // Given
      Artifact old = new Artifact();
      old.setId("12302");
      old.setName("Second Artifact");
      old.setDescription("Second Artifact get small");
      old.setImageUrl("image");

      Artifact update = new Artifact();
      update.setId("12302");
      update.setName("update Artifact");
      update.setDescription("update Second Artifact get small");
      update.setImageUrl("update image");
      update.setOwner(null);
      given(artifactRepository.findById("12302")).willReturn(Optional.of(old));
      given(artifactRepository.save(Mockito.any(Artifact.class))).willReturn(old);
      // When
      Artifact add = artifactService.update("12302", update);
      // Then
      assertThat(add.getId()).isEqualTo("12302");
      assertThat(add.getName()).isEqualTo("update Artifact");
      assertThat(add.getDescription()).isEqualTo("update Second Artifact get small");
      verify(artifactRepository, times(1)).save(add);
   }

   @Test
   void testUpdateNotFound() {
      // Given
      Artifact old = new Artifact();
      old.setId("12302");
      old.setName("Second Artifact");
      old.setDescription("Second Artifact get small");
      old.setImageUrl("image");

      given(artifactRepository.findById(Mockito.any(String.class))).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         Artifact add = artifactService.update("12302", old);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find artifact with id 12302");
      verify(artifactRepository, times(1)).findById("12302");
   }

   @Test
   void testDeleteSuccess() {
      // Given
      Artifact art = new Artifact();
      art.setId("12302");
      art.setName("Second Artifact");
      art.setDescription("Second Artifact get small");
      art.setImageUrl("image");

      given(artifactRepository.findById("12302")).willReturn(Optional.of(art));
      doNothing().when(artifactRepository).deleteById("12302");
      // When
      artifactService.delete("12302");
      // Then
      verify(artifactRepository, times(1)).deleteById("12302");
   }

   @Test
   void testDeleteNotFound() {
      // Given
      Artifact art = new Artifact();
      art.setId("12302");
      art.setName("Second Artifact");
      art.setDescription("Second Artifact get small");
      art.setImageUrl("image");

      given(artifactRepository.findById("12302")).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         Artifact artifact = artifactService.findById("12302");
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
              .hasMessage("Could not find artifact with id 12302");
      verify(artifactRepository, times(1)).findById("12302");
   }

   @Test
   void testGenerateSuccess() throws JsonProcessingException {
      // Given
      WizardDto wizard = new WizardDto(6,"마술사",
              LocalDateTime.of(1991, 2,4,2,2,1,333), 2);
      List<ArtifactDto> artifactDtos = List.of(
              new ArtifactDto("45601", "숨기", "기술", "imageUrl",
                      LocalDateTime.of(1991, 2,4,2,2,1,333), wizard),
              new ArtifactDto("45602", "size up", "bit size", "imageUrl",
                      LocalDateTime.of(1991, 2,4,2,2,1,333), wizard)
      );
      // LocalDateTime이 있으면 pom.xml에 dependency 추가하고 regitsterModule() 추가해야함
      ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
      String json = mapper.writeValueAsString(artifactDtos);

      ChatRequest chatRequest = new ChatRequest("gpt-4", List.of(
              new Message("system", "질문할 내용"),
              new Message("user", json)
              ));

      ChatResponse chatResponse = new ChatResponse(List.of(
              new Choice(0, new Message("assistant", "질문에 대한 답변"))));
      given(chatClient.generate(chatRequest)).willReturn(chatResponse);
      // When
      String summary = artifactService.summarize(artifactDtos);

      // Then
      assertThat(summary).isEqualTo("질문에 대한 답변");
      verify(chatClient, times(1)).generate(chatRequest);
   }
}













