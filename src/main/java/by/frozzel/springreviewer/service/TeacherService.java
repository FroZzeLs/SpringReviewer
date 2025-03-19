package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.dao.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TeacherService {
    private final TeacherRepository teacherRepository;

    public TeacherService(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public Teacher getTeacherBySurnameAndName(String surname, String name) {
        return teacherRepository.findBySurnameAndName(surname, name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Преподаватель не найден"));
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public Teacher updateTeacher(String surname, String name, Teacher updatedTeacher) {
        Teacher existingTeacher = getTeacherBySurnameAndName(surname, name);
        existingTeacher.setName(updatedTeacher.getName());
        existingTeacher.setSurname(updatedTeacher.getSurname());
        existingTeacher.setPatronym(updatedTeacher.getPatronym());
        return teacherRepository.save(existingTeacher);
    }

    public void deleteTeacher(String surname, String name) {
        Teacher teacher = getTeacherBySurnameAndName(surname, name);
        teacherRepository.delete(teacher);
    }
}
