package kr.masul.artifact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ArtifactRepository extends JpaRepository<Artifact, String>,
        JpaSpecificationExecutor<Artifact> {

/*
   List<Artifact> findByIdAndNameContainingAndDescriptionContainingAndCreateAtBetweenOrderByNameAsc(
           String id, String name, String description,
           LocalDateTime start, LocalDateTime end
   );
*/

   /**
    * hasId, containsName, containsDescription, hasOwnerName
    */
}
