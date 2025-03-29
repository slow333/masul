package kr.masul.artifact;

import jakarta.transaction.Transactional;
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

   public Artifact findById(String artifactId) {
      return artifactRepository
              .findById(artifactId).orElseThrow(() -> new ObjectNotFoundException("artifact",artifactId));

   }

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
}
