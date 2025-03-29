package kr.masul.artifact;

import jakarta.validation.Valid;
import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import kr.masul.system.converter.ArtifactToDto;
import kr.masul.system.converter.ArtifactToEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base-url}/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private final ArtifactService artifactService;
    private final ArtifactToDto artifactToDto;
    private final ArtifactToEntity artifactToEntity;

    @GetMapping("/{artifactId}")
    public Result findById(@PathVariable String artifactId) {
        Artifact artifact = artifactService.findById(artifactId);
        ArtifactDto artifactDto = artifactToDto.convert(artifact);
        return new Result(true, StatusCode.SUCCESS, "Find Success", artifactDto);
    }

    @GetMapping
    public Result findAll() {
        List<Artifact> artifacts = artifactService.findAll();
        List<ArtifactDto> artifactDtos = artifacts.stream().map(artifactToDto::convert).toList();
        return new Result(true, StatusCode.SUCCESS, "Find all Success", artifactDtos);
    }

    @PostMapping
    public Result add(@Valid @RequestBody ArtifactDto artifactDto) {
        Artifact a = artifactToEntity.convert(artifactDto);
        Artifact addedArtifact = artifactService.add(a);
        ArtifactDto dto = artifactToDto.convert(addedArtifact);
        return new Result(true, StatusCode.SUCCESS, "Add Success", dto);
    }
    @PutMapping("/{artifactId}")
    public Result update(@PathVariable String artifactId, @RequestBody ArtifactDto artifactDto) {
        Artifact art = artifactToEntity.convert(artifactDto);
        Artifact update = artifactService.update(artifactId, art);
        ArtifactDto dto = artifactToDto.convert(update);
        return new Result(true, StatusCode.SUCCESS, "Update Success", dto);
    }
    @DeleteMapping("/{artifactId}")
    public Result delete(@PathVariable String artifactId) {
        artifactService.delete(artifactId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }
}
