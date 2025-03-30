package kr.masul.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<MaUser, Integer> {
   Optional<MaUser> findByUsername(String username);
}
