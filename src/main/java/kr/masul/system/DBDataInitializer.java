package kr.masul.system;

import kr.masul.artifact.Artifact;
import kr.masul.artifact.ArtifactRepository;
import kr.masul.wizard.Wizard;
import kr.masul.wizard.WizardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DBDataInitializer implements CommandLineRunner {

    private final ArtifactRepository artifactRepository;
    private final WizardRepository wizardRepository;
    private final IdWorker idWorker;

    @Override
    public void run(String... args) throws Exception {

        Artifact a1 = new Artifact();
        a1.setId("12301");
        a1.setName("First Artifact");
        a1.setDescription("First Artifact hide");
        a1.setCreateAt(LocalDateTime.now());
        a1.setImageUrl("image");

        Artifact a2 = new Artifact();
        a2.setId("12302");
        a2.setName("Second Artifact");
        a2.setDescription("Second Artifact get small");
        a2.setCreateAt(LocalDateTime.now());
        a2.setImageUrl("image");

        Artifact a3 = new Artifact();
        a3.setId("12303");
        a3.setName("Third Artifact");
        a3.setDescription("Third Artifact get large");
        a3.setCreateAt(LocalDateTime.now());
        a3.setImageUrl("image");

        Artifact a4 = new Artifact();
        a4.setId("12304");
        a4.setName("Fourth Artifact");
        a4.setDescription("Fourth Artifact fly");
        a4.setCreateAt(LocalDateTime.now());
        a4.setImageUrl("image");

        Artifact a5 = new Artifact();
        a5.setId("12305");
        a5.setName("Fifth Artifact");
        a5.setDescription("Fifth Artifact money");
        a5.setCreateAt(LocalDateTime.now());
        a5.setImageUrl("image");

        Artifact a6 = new Artifact();
        a6.setId("12306");
        a6.setName("Sixth Artifact");
        a6.setDescription("Sixth Artifact brain");
        a6.setCreateAt(LocalDateTime.now());
        a6.setImageUrl("image");

        Artifact a7 = new Artifact();
        a7.setId(idWorker.nextId()+"");
        a7.setName("7th Artifact");
        a7.setDescription("7th Artifact culture");
        a7.setCreateAt(LocalDateTime.now());
        a7.setImageUrl("image");

        Artifact a8 = new Artifact();
        a8.setId(idWorker.nextId()+"");
        a8.setName("8th Artifact");
        a8.setDescription("8th Artifact keyboard");
        a8.setCreateAt(LocalDateTime.now());
        a8.setImageUrl("image");

        Wizard w1 = new Wizard();
        w1.setName("SuperMan");
        w1.setBirthday(LocalDateTime.of(1901, 2, 24, 18, 30));
        w1.addArtifact(a1);
        w1.addArtifact(a2);

        Wizard w2 = new Wizard();
        w2.setName("WonderWoman");
        w2.setBirthday(LocalDateTime.of(1730, 10, 22, 4, 51));
        w2.addArtifact(a3);
        w2.addArtifact(a4);

        Wizard w3 = new Wizard();
        w3.setName("SpiderMan");
        w3.setBirthday(LocalDateTime.of(2001, 7, 1, 6, 10));
        w3.addArtifact(a5);

        this.wizardRepository.save(w1);
        this.wizardRepository.save(w2);
        this.wizardRepository.save(w3);

        this.artifactRepository.save(a6);
        this.artifactRepository.save(a7);
        this.artifactRepository.save(a8);
    }
}
