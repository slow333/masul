package kr.masul.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class MaUser {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @NotEmpty(message = "Username is required.")
   private String username;

   @NotEmpty(message = "Password is required.")
   private String password;

   private boolean enabled;

   @NotEmpty(message = "Roles is required.")
   private String roles;
}
