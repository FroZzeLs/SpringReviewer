package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    public ResponseEntity<SubjectDisplayDto> createSubject(@RequestBody SubjectCreateDto dto) {
        return ResponseEntity.ok(subjectService.createSubject(dto));
    }

    @GetMapping
    public ResponseEntity<List<SubjectDisplayDto>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDisplayDto> getSubjectById(@PathVariable Integer id) {
        return subjectService.getSubjectById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<SubjectDisplayDto> getSubjectByName(@PathVariable String name) {
        return subjectService.getSubjectByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectDisplayDto> updateSubject(@PathVariable Integer id, @RequestBody SubjectCreateDto dto) {
        return subjectService.updateSubject(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubject(@PathVariable Integer id) {
        return subjectService.deleteSubject(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
