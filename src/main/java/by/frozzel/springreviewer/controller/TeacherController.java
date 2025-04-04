package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.service.TeacherService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherController {
    private final TeacherService teacherService;

    @GetMapping
    public List<TeacherDisplayDto> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    @GetMapping("/{id}")
    public TeacherDisplayDto getTeacherById(@PathVariable Integer id) {
        return teacherService.getTeacherById(id);
    }

    @GetMapping("/search/by-fullname")
    public TeacherDisplayDto getTeacherByFullName(@RequestParam String surname,
                                                  @RequestParam String name) {
        return teacherService.getTeacherByFullName(surname, name);
    }

    @GetMapping("/search/by-subject")
    public List<TeacherDisplayDto> getTeachersBySubjectName(@RequestParam String subjectName) {
        return teacherService.getTeachersBySubjectName(subjectName);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDisplayDto createTeacher(@RequestBody TeacherCreateDto teacherCreateDto) {
        return teacherService.createTeacher(teacherCreateDto);
    }

    @PutMapping("/{id}")
    public TeacherDisplayDto updateTeacher(@PathVariable Integer id,
                                           @RequestBody TeacherCreateDto teacherCreateDto) {
        return teacherService.updateTeacher(id, teacherCreateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacher(@PathVariable Integer id) {
        teacherService.deleteTeacher(id);
    }

    @PostMapping("/{teacherId}/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignSubjectToTeacher(
            @PathVariable int teacherId,
            @PathVariable int subjectId) {
        teacherService.assignSubjectToTeacher(teacherId, subjectId);
    }

    @DeleteMapping("/{teacherId}/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSubjectFromTeacher(@PathVariable int teacherId,
                                         @PathVariable int subjectId) {
        teacherService.removeSubjectFromTeacher(teacherId, subjectId);
    }
}