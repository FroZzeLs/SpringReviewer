package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.service.SubjectService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Validated
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectDisplayDto createSubject(@Valid @RequestBody SubjectCreateDto dto) {
        return subjectService.createSubject(dto);
    }

    @GetMapping
    public List<SubjectDisplayDto> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{id}")
    public SubjectDisplayDto getSubjectById(@PathVariable @Min(value = 1,
            message = "Subject ID must be positive") Integer id) {
        return subjectService.getSubjectById(id);
    }

    @GetMapping("/name/{name}")
    public SubjectDisplayDto getSubjectByName(
            @PathVariable @NotBlank(message = "Subject name cannot be blank") String name) {
        return subjectService.getSubjectByName(name);
    }

    @PutMapping("/{id}")
    public SubjectDisplayDto updateSubject(
            @PathVariable @Min(value = 1, message = "Subject ID must be positive") Integer id,
                                           @Valid @RequestBody SubjectCreateDto dto) {
        return subjectService.updateSubject(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubject(@PathVariable @Min(value = 1,
            message = "Subject ID must be positive") Integer id) {
        subjectService.deleteSubject(id);
    }
}