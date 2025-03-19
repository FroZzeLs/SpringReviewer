package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teachers")
public class TeacherController {
    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public List<Teacher> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherBySurnameAndName(@RequestParam String surname, @RequestParam String name) {
        return ResponseEntity.ok(teacherService.getTeacherBySurnameAndName(surname, name));
    }

    @PostMapping
    public ResponseEntity<Teacher> createTeacher(@RequestBody Teacher teacher) {
        return ResponseEntity.ok(teacherService.createTeacher(teacher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@RequestParam String surname, @RequestParam String name, @RequestBody Teacher teacher) {
        return ResponseEntity.ok(teacherService.updateTeacher(surname, name, teacher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@RequestParam String surname, @RequestParam String name) {
        teacherService.deleteTeacher(surname, name);
        return ResponseEntity.noContent().build();
    }
}
