package kr.masul.wizard;

import jakarta.validation.Valid;
import kr.masul.system.Result;
import kr.masul.system.StatusCode;
import kr.masul.system.converter.WizardToDto;
import kr.masul.system.converter.WizardToEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base-url}/wizards")
@RequiredArgsConstructor
public class WizardController {

   private final WizardService wizardService;
   private final WizardToDto wizardToDto;
   private final WizardToEntity wizardToEntity;

   @GetMapping("/{wizardId}")
   public Result findById(@PathVariable Integer wizardId) {
      Wizard wizard = wizardService.findById(wizardId);
      WizardDto dto = wizardToDto.convert(wizard);
      return new Result(true, StatusCode.SUCCESS, "Find Success", dto);
   }

   @GetMapping
   public Result findAll() {
      List<Wizard> wizards = wizardService.findAll();
      List<WizardDto> dtos = wizards.stream().map(wizardToDto::convert).toList();
      return new Result(true, StatusCode.SUCCESS, "Find all Success", dtos);
   }

   @PostMapping
   public Result add(@RequestBody WizardDto wizardDto){
      Wizard foundWizard = wizardToEntity.convert(wizardDto);
      Wizard convertWizard = wizardService.add(foundWizard);
      WizardDto dto = wizardToDto.convert(convertWizard);
      return new Result(true, StatusCode.SUCCESS, "Add Success", dto);
   }

   @PutMapping("/{wizardId}")
   public Result update(@PathVariable Integer wizardId, @Valid @RequestBody WizardDto wizardDto) {
      Wizard convertWizard = wizardToEntity.convert(wizardDto);
      Wizard wizard = wizardService.update(wizardId, convertWizard);
      WizardDto dto = wizardToDto.convert(wizard);
      return new Result(true, StatusCode.SUCCESS, "Update Success", dto);
   }

   @DeleteMapping("/{wizardId}")
   public Result delete(@PathVariable Integer wizardId) {
      wizardService.delete(wizardId);
      return new Result(true, StatusCode.SUCCESS, "Delete Success");
   }
   @PutMapping("/{wizardId}/artifacts/{artifactId}")
   public Result assignArtifact(@PathVariable Integer wizardId,
                                @PathVariable String artifactId) {
      wizardService.assignArtifact(wizardId, artifactId);
      return new Result(true, StatusCode.SUCCESS, "Assign artifact Success");
   }
}

