package kr.masul.artifact;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import kr.masul.client.imageStorage.ImageStorageClient;
import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import kr.masul.system.converter.ArtifactToDto;
import kr.masul.system.converter.ArtifactToEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.base-url}/artifacts")
@RequiredArgsConstructor
public class ArtifactController {

    private final ArtifactService artifactService;
    private final ArtifactToDto artifactToDto;
    private final ArtifactToEntity artifactToEntity;
    private final MeterRegistry meterRegistry; // actuator에 metircs 값으로 가져오기 위해 필요
    private final ImageStorageClient imageStrorageClient;

    @GetMapping("/{artifactId}")
    public Result findById(@PathVariable String artifactId) {
        Artifact artifact = artifactService.findById(artifactId);
        meterRegistry.counter("artifact.id." + artifactId).increment(); // 조회수를 가지고 옮
//        meterRegistry.gauge("artifact_count", 13);
//        meterRegistry.timer("artifact_findById", "timer");
        ArtifactDto artifactDto = artifactToDto.convert(artifact);

        return new Result(true, StatusCode.SUCCESS, "Find Success", artifactDto);
    }

    /**
     * spring.data.web.pageable.page-parameter = 페이지 사용자정의이름(default; page)
     * spring.data.web.pageable.size-parameter = size 사용자정의이름(default; size)
     * spring.data.web.sort.sort-parameter = sort 사용자정의이름
     * 요청 ; http://localhost:80/api/v1/artifacts?size=1&page=2&sort=name,asc
     * @param pageable
     * @return
     */
    @GetMapping
    public Result findAll(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
         // frontend에서 파라미터 변수로 page, size, sort를 받아서 처리할 때는 아래 처럼 해서 처리해야함
        // findAll(@RequestParam(default=0) int page,
        //         @RequestParam(default=10) int size,
        //         @RequestParam(name="sort") List<String> sort)
         // List<Sort.Order> sorts = List.of(Sort.Order.desc("name"));
         // pageable = PageRequest.of(0, 2, Sort.by(sorts));

        Page<Artifact> artifactPage = artifactService.findAll(pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage // Page streamable no need stream()
                .map(artifactToDto::convert);

        return new Result(true, StatusCode.SUCCESS, "Find all Success", artifactDtoPage);
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
        Artifact artifact = artifactToEntity.convert(artifactDto);
        Artifact update = artifactService.update(artifactId, artifact);
        ArtifactDto dto = artifactToDto.convert(update);

        return new Result(true, StatusCode.SUCCESS, "Update Success", dto);
    }

    @DeleteMapping("/{artifactId}")
    public Result delete(@PathVariable String artifactId) {
        artifactService.delete(artifactId);
        return new Result(true, StatusCode.SUCCESS, "Delete Success");
    }

    @GetMapping("/summary")
    public Result summarizeArtifact() throws JsonProcessingException {

        List<Artifact> artifacts = artifactService.findAll();
        List<ArtifactDto> artifactDtos = artifacts.stream().map(artifactToDto::convert).toList();

        String summarize = this.artifactService.summarize(artifactDtos);

        return new Result(true, StatusCode.SUCCESS, "Summarize Success", summarize);
    }

    @PostMapping("/search")
    public Result findArtifactByCriteria(
            @RequestBody Map<String, String> searchCriteria ,
//            @PageableDefault(page = 0, size = 3, direction = Sort.Direction.ASC, sort = "name")
            Pageable pageable){
        Page<Artifact> artifactPage = artifactService.findByCriteria(searchCriteria, pageable);
        Page<ArtifactDto> artifactDtoPage = artifactPage.map(artifactToDto::convert);

        return new Result(true, StatusCode.SUCCESS, "Search Success", artifactDtoPage);
    }

    @PostMapping("/images")
    public Result uploadImage(
            @RequestParam String containerName, @RequestParam MultipartFile file) throws IOException {
        try(InputStream inputStream = file.getInputStream()) {
            String imageUrl = imageStrorageClient.uploadImage(containerName, file.getOriginalFilename(), inputStream, file.getSize());
            return new Result(true, StatusCode.SUCCESS, "Upload Image Success", imageUrl);
        }
    }
}























