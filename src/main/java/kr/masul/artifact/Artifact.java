package kr.masul.artifact;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import kr.masul.wizard.Wizard;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity
@Getter
@Setter
public class Artifact {

    @Id
    private String id;

    private String name;

    private String description;

    private String imageUrl;

    private LocalDateTime createAt;

    @ManyToOne
    private Wizard owner;
}
