package kr.masul.wizard;

import jakarta.transaction.Transactional;
import kr.masul.artifact.Artifact;
import kr.masul.artifact.ArtifactRepository;
import kr.masul.system.exception.ObjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WizardService {

   private final WizardRepository wizardRepository;
   private final ArtifactRepository artifactRepository;

   public Wizard findById(Integer wizardId) {
      return wizardRepository.findById(wizardId)
              .orElseThrow(() -> new ObjectNotFoundException("wizard",wizardId));
   }

   public List<Wizard> findAll() {
      return wizardRepository.findAll();
   }

   public Wizard add(Wizard wizard) {
      return wizardRepository.save(wizard);
   }

   public Wizard update(Integer wizardId, Wizard wizard) {
      Wizard oldWizard = wizardRepository.findById(wizardId)
              .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));
      oldWizard.setId(wizard.getId());
      oldWizard.setName(wizard.getName());
      oldWizard.setBirthday(wizard.getBirthday());
      oldWizard.setArtifacts(wizard.getArtifacts());
      return oldWizard;
   }

   public void delete(Integer wizardId) {
      Wizard wizard = wizardRepository.findById(wizardId)
              .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

      wizard.removeAllArtifacts();
      wizardRepository.deleteById(wizardId);
   }

   public void assignArtifact(Integer wizardId, String artifactId) {
      Artifact artifact = artifactRepository.findById(artifactId)
              .orElseThrow(() -> new ObjectNotFoundException("artifact", artifactId));

      Wizard wizard = wizardRepository.findById(wizardId)
              .orElseThrow(() -> new ObjectNotFoundException("wizard", wizardId));

      if (artifact.getOwner() != null) {
         artifact.getOwner().removeArtifact(artifact);
      }
      wizard.addArtifact(artifact);
   }
}
