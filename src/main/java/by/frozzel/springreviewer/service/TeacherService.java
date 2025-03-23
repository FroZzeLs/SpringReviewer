package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.mapper.TeacherMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherMapper teacherMapper;
    private static final String TEACHER_NOT_FOUND_MESSAGE = "Teacher not found";
    private static final String SUBJECT_NOT_FOUND_MESSAGE = "Subject not found";

    public List<TeacherDisplayDto> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toDto)
                .toList();
    }

    public TeacherDisplayDto getTeacherById(Integer id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(TEACHER_NOT_FOUND_MESSAGE));
        return teacherMapper.toDto(teacher);
    }

    public TeacherDisplayDto createTeacher(TeacherCreateDto teacherCreateDto) {
        Teacher teacher = teacherMapper.toEntity(teacherCreateDto);
        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    public TeacherDisplayDto updateTeacher(Integer id, TeacherCreateDto teacherCreateDto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(TEACHER_NOT_FOUND_MESSAGE));
        teacher.setSurname(teacherCreateDto.getSurname());
        teacher.setName(teacherCreateDto.getName());
        teacher.setPatronym(teacherCreateDto.getPatronym());
        return teacherMapper.toDto(teacherRepository.save(teacher));
    }

    public void deleteTeacher(Integer id) {
        teacherRepository.deleteById(id);
    }

    public TeacherDisplayDto getTeacherByFullName(String surname, String name) {
        Teacher teacher = teacherRepository.findBySurnameAndName(surname, name)
                .orElseThrow(() -> new RuntimeException(TEACHER_NOT_FOUND_MESSAGE));
        return teacherMapper.toDto(teacher);
    }

    @Transactional
    public void assignSubjectToTeacher(int teacherId, int subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new EntityNotFoundException(TEACHER_NOT_FOUND_MESSAGE));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new EntityNotFoundException(SUBJECT_NOT_FOUND_MESSAGE));

        if (!teacher.getSubjects().contains(subject)) {
            teacher.getSubjects().add(subject);
            teacherRepository.save(teacher);
        }
    }

    @Transactional
    public void removeSubjectFromTeacher(int teacherId, int subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TEACHER_NOT_FOUND_MESSAGE));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, SUBJECT_NOT_FOUND_MESSAGE));

        if (!teacher.getSubjects().contains(subject)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Teacher does not teach this subject");
        }

        teacher.getSubjects().remove(subject);
        teacherRepository.save(teacher);
    }
}
