package kr.masul.wizard;

import kr.masul.artifact.Artifact;
import kr.masul.artifact.ArtifactRepository;
import kr.masul.system.exception.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class WizardServiceTest {

   @Mock
   WizardRepository wizardRepository;
   @Mock
   ArtifactRepository artifactRepository;

   @InjectMocks
   WizardService wizardService;

   List<Wizard> wizardList;

   @BeforeEach
   void setUp() {
      wizardList = new ArrayList<>();
      Wizard w1 = new Wizard();
      w1.setName("SuperMan");

      Wizard w2 = new Wizard();
      w2.setName("WonderWoman");

      Wizard w3 = new Wizard();
      w3.setName("SpiderMan");

      wizardList.add(w1);
      wizardList.add(w2);
      wizardList.add(w3);
   }

   @Test
   @DisplayName("wizard find by id success")
   void testFindByIdSuccess() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.findById(2)).willReturn(Optional.of(w1));
      // When
      Wizard wizard = wizardService.findById(2);
      // Then
      assertThat(wizard.getId()).isEqualTo(2);
      assertThat(wizard.getName()).isEqualTo("SuperMan");
      verify(wizardRepository, times(1)).findById(2);
   }
   @Test
   @DisplayName("wizard find by id fail")
   void testFindByIdNotFound() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.findById(2)).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         Wizard wizard = wizardService.findById(2);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find wizard with id 2");
      verify(wizardRepository, times(1)).findById(2);
   }


   @Test
   void testFindAllSuccess() {
      // Given
      given(wizardRepository.findAll()).willReturn(wizardList);
      // When
      List<Wizard> all = wizardService.findAll();
      // Then
      assertThat(all.size()).isEqualTo(3);
      verify(wizardRepository, times(1)).findAll();
   }

   @Test
   void testAddSuccess() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.save(w1)).willReturn(w1);
      // When
      Wizard add = wizardService.add(w1);
      // Then
      assertThat(add.getId()).isEqualTo(2);
      assertThat(add.getName()).isEqualTo("SuperMan");
   }

   @Test
   void testAddNotFound() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.save(w1)).willThrow(new ObjectNotFoundException("wizard", 2));
      // When
      Throwable thrown = catchThrowable(() -> {
         Wizard add = wizardService.add(w1);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find wizard with id 2");
   }

   @Test
   void testUpdateSuccess() {
      // Given
       Wizard w1 = new Wizard();
       w1.setId(2);
       w1.setName("SuperMan");

      given(wizardRepository.save(w1)).willReturn(w1);
      // When
      Wizard add = wizardService.add(w1);
      // Then
      assertThat(add.getId()).isEqualTo(2);
      assertThat(add.getName()).isEqualTo("SuperMan");
   }

   @Test
   void testUpdateNotFound() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.save(w1)).willThrow(new ObjectNotFoundException("wizard", 2));
      // When
      Throwable thrown = catchThrowable(() -> {
         Wizard add = wizardService.add(w1);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find wizard with id 2");
   }

   @Test
   void testDeleteSuccess() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.findById(2)).willReturn(Optional.of(w1));
      doNothing().when(wizardRepository).deleteById(2);
      // When
      wizardService.delete(2);
      // Then
      verify(wizardRepository, times(1)).deleteById(2);
   }

   @Test
   void testDeleteNotFound() {
      // Given
      Wizard w1 = new Wizard();
      w1.setId(2);
      w1.setName("SuperMan");

      given(wizardRepository.findById(2)).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         wizardService.findById(2);
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
              .hasMessage("Could not find wizard with id 2");
   }

   @Test
   @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
   void testAssignSuccess() {
      // Given
      Artifact a = new Artifact();
      a.setId("12306");
      a.setName("Sixth Artifact");
      a.setDescription("Sixth Artifact brain");
      a.setImageUrl("image");

      Wizard w = new Wizard();
      w.setId(2);
      w.setName("SuperMan");
      w.addArtifact(a);

      Wizard w3 = new Wizard();
      w3.setId(3);
      w3.setName("Neville Longbottom");

      given(artifactRepository.findById("12306")).willReturn(Optional.of(a));
      given(wizardRepository.findById(3)).willReturn(Optional.of(w3));
      // When
      wizardService.assignArtifact(3, "12306");
      // Then
      assertThat(a.getOwner().getId()).isEqualTo(3);
      assertThat(w3.getArtifacts().size()).isEqualTo(1);
   }

   @Test
   @DisplayName("assign error not found artifact")
   void testAssignNotFoundWizard() {
      // Given
      Artifact a = new Artifact();
      a.setId("12306");
      a.setName("Sixth Artifact");
      a.setDescription("Sixth Artifact brain");
      a.setImageUrl("image");

      Wizard w = new Wizard();
      w.setId(2);
      w.setName("SuperMan");

      given(wizardRepository.findById(2)).willReturn(Optional.empty());
      given(artifactRepository.findById("12306")).willReturn(Optional.of(a));
      // When
      Throwable thrown = catchThrowable(() -> {
         wizardService.assignArtifact(2, "12306");
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class).hasMessage("Could not find wizard with id 2");
      verify(wizardRepository, times(1)).findById(2);
   }

   @Test
   @DisplayName("assign error not found artifact")
   void testAssignNotFoundArtifact() {
      // Given
      Wizard w = new Wizard();
      w.setId(2);
      w.setName("SuperMan");

      given(artifactRepository.findById("12309")).willReturn(Optional.empty());
      // When
      Throwable thrown = catchThrowable(() -> {
         wizardService.assignArtifact(2, "12309");
      });
      // Then
      assertThat(thrown).isInstanceOf(ObjectNotFoundException.class)
              .hasMessage("Could not find artifact with id 12309");
   }

}