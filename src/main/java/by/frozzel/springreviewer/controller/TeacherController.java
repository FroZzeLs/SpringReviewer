package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.service.TeacherService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Validated
public class TeacherController {
    private final TeacherService teacherService;

    @GetMapping
    public List<TeacherDisplayDto> getAllTeachers() {
        return teacherService.getAllTeachers();
    }

    @GetMapping("/{id}")
    public TeacherDisplayDto getTeacherById(
            @PathVariable @Min(value = 1, message = "Teacher ID must be positive") Integer id) {
        return teacherService.getTeacherById(id);
    }

    @GetMapping("/search/by-fullname")
    public TeacherDisplayDto getTeacherByFullName(
            @RequestParam @NotBlank(message = "Surname cannot be blank")
            @Size(max = 50) String surname,
            @RequestParam @NotBlank(message = "Name cannot be blank")
            @Size(max = 50) String name) {
        return teacherService.getTeacherByFullName(surname, name);
    }

    @GetMapping("/search/by-subject")
    public List<TeacherDisplayDto> getTeachersBySubjectName(
            @RequestParam @NotBlank(message = "Subject name cannot be blank") String subjectName) {
        return teacherService.getTeachersBySubjectName(subjectName);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeacherDisplayDto createTeacher(@Valid @RequestBody TeacherCreateDto teacherCreateDto) {
        return teacherService.createTeacher(teacherCreateDto);
    }

    @PutMapping("/{id}")
    public TeacherDisplayDto updateTeacher(
            @PathVariable @Min(value = 1, message = "Teacher ID must be positive") Integer id,
                                           @Valid @RequestBody TeacherCreateDto teacherCreateDto) {
        return teacherService.updateTeacher(id, teacherCreateDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTeacher(@PathVariable @Min(value = 1,
            message = "Teacher ID must be positive") Integer id) {
        teacherService.deleteTeacher(id);
    }

    @PostMapping("/{teacherId}/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assignSubjectToTeacher(
            @PathVariable @Min(value = 1, message = "Teacher ID must be positive") int teacherId,
            @PathVariable @Min(value = 1, message = "Subject ID must be positive") int subjectId) {
        teacherService.assignSubjectToTeacher(teacherId, subjectId);
    }

    @DeleteMapping("/{teacherId}/subjects/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSubjectFromTeacher(
            @PathVariable @Min(value = 1, message = "Teacher ID must be positive") int teacherId,
            @PathVariable @Min(value = 1, message = "Subject ID must be positive") int subjectId) {
        teacherService.removeSubjectFromTeacher(teacherId, subjectId);
    }
}