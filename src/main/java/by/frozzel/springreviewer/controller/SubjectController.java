package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.service.SubjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping
    public List<Subject> getAllSubjects() {
        return subjectService.getAllSubjects();
    }

    @GetMapping("/{name}")
    public ResponseEntity<Subject> getSubjectByName(@PathVariable String name) {
        return ResponseEntity.ok(subjectService.getSubjectByName(name));
    }

    @PostMapping
    public ResponseEntity<Subject> createSubject(@RequestBody Subject subject) {
        return ResponseEntity.ok(subjectService.createSubject(subject));
    }

    @PutMapping("/{name}")
    public ResponseEntity<Subject> updateSubject(@PathVariable String name, @RequestBody Subject subject) {
        return ResponseEntity.ok(subjectService.updateSubject(name, subject));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteSubject(@PathVariable String name) {
        subjectService.deleteSubject(name);
        return ResponseEntity.noContent().build();
    }
}
