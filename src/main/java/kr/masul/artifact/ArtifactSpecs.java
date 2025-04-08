package kr.masul.artifact;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ArtifactSpecs {

   public static Specification<Artifact> hasId(String providedId) {
      return (root, query, criteriaBuilder) ->
              criteriaBuilder.equal(root.get("id"), providedId);
   }

   public static Specification<Artifact> containsName(String providedName) {
      return (root, query, criteriaBuilder) ->
              criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                      "%" + providedName.toLowerCase() + "%");
   }

   public static Specification<Artifact> containsDescription(String providedDescription) {
      return (root, query, criteriaBuilder) ->
              // "_" 한개 match, "%" 여러개 매치, 모두 소문자로 변경
              // root = artifact, select * from artifact where id=providedId, from에 해당함
              criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                      "%" + providedDescription.toLowerCase() + "%");
   }

   public static Specification<Artifact> hasOwnerName(String providedOwnerName) {
      return (root, query, criteriaBuilder) ->
              criteriaBuilder.equal(criteriaBuilder.lower(root.get("owner").get("name")),
                      providedOwnerName.toLowerCase());
   }

   public static Specification<Artifact> containsEmail(String providedEmail) {

      return new Specification<Artifact>() {
         @Override
         public Predicate toPredicate(Root<Artifact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            return criteriaBuilder.like(root.get("email"), providedEmail+"%");
         }
      };
   }
   public static Specification<Artifact> betweenDateTime(String start, String end) {

      return new Specification<Artifact>() {
         @Override
         public Predicate toPredicate(Root<Artifact> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            return criteriaBuilder.between(root.get("createAt"), start, end);
         }
      };
   }
}