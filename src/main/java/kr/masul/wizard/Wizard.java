package kr.masul.wizard;

import jakarta.persistence.*;
import kr.masul.artifact.Artifact;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Wizard {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    private LocalDateTime birthday;

    @OneToMany(mappedBy = "owner", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Artifact> artifacts = new ArrayList<>();

    public void addArtifact(Artifact artifact) {
        artifact.setOwner(this);
        artifacts.add(artifact);
    }

    public Integer getNumberOfArtifacts() {
        return artifacts.size();
    }

    public void removeArtifact(Artifact artifact) {
        artifact.setOwner(null);
        artifacts.remove(artifact);
    }

    public void removeAllArtifacts() {
        artifacts.stream().forEach(a -> a.setOwner(null));
        artifacts = new ArrayList<>();
    }
}
