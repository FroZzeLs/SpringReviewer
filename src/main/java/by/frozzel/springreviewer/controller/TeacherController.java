package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.service.TeacherService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/teachers")
    public List<Teacher> getTeachersBySubject(@RequestParam String subject) {
        return teacherService.getTeachersBySubject(subject);
    }

    @GetMapping("/teachers/{surname}")
    public Teacher getTeacherBySurname(@PathVariable String surname) {
        return Teacher.builder().surname(surname).name("Юрий")
                .patronym("Александрович").subject("АиЛОЦУ").build();
    }
}