package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.mapper.TeacherMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherMapper teacherMapper;

    private static final String TEACHER_NOT_FOUND_MESSAGE = "Teacher not found with ID: ";
    private static final String TEACHER_BY_NAME_NOT_FOUND_MESSAGE = "Teacher "
            + "not found with surname: %s and name: %s";
    private static final String SUBJECT_NOT_FOUND_MESSAGE = "Subject"
            + " not found with ID: ";

    @Transactional(readOnly = true)
    public List<TeacherDisplayDto> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TeacherDisplayDto getTeacherById(Integer id) {
        return teacherRepository.findById(id)
                .map(teacherMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        TEACHER_NOT_FOUND_MESSAGE + id));
    }

    @Transactional
    public TeacherDisplayDto createTeacher(TeacherCreateDto teacherCreateDto) {
        Teacher teacher = teacherMapper.toEntity(teacherCreateDto);
        Teacher savedTeacher = teacherRepository.save(teacher);
        return teacherMapper.toDto(savedTeacher);
    }

    @Transactional
    public TeacherDisplayDto updateTeacher(Integer id, TeacherCreateDto teacherCreateDto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        TEACHER_NOT_FOUND_MESSAGE + id));
        teacher.setSurname(teacherCreateDto.getSurname());
        teacher.setName(teacherCreateDto.getName());
        teacher.setPatronym(teacherCreateDto.getPatronym());
        Teacher updatedTeacher = teacherRepository.save(teacher);
        return teacherMapper.toDto(updatedTeacher);
    }

    @Transactional
    public void deleteTeacher(Integer id) {
        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TEACHER_NOT_FOUND_MESSAGE + id);
        }
    }

    @Transactional(readOnly = true)
    public TeacherDisplayDto getTeacherByFullName(String surname, String name) {
        return teacherRepository.findBySurnameAndName(surname, name)
                .map(teacherMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format(TEACHER_BY_NAME_NOT_FOUND_MESSAGE, surname, name)));
    }

    @Transactional
    public void assignSubjectToTeacher(int teacherId, int subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        TEACHER_NOT_FOUND_MESSAGE + teacherId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        SUBJECT_NOT_FOUND_MESSAGE + subjectId));

        if (teacher.getSubjects().add(subject)) {
            teacherRepository.save(teacher);
        } else {
            log.info("Teacher {} already teaches subject {}", teacherId, subjectId);
        }
    }

    @Transactional
    public void removeSubjectFromTeacher(int teacherId, int subjectId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        TEACHER_NOT_FOUND_MESSAGE + teacherId));

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        SUBJECT_NOT_FOUND_MESSAGE + subjectId));

        if (teacher.getSubjects().remove(subject)) {
            teacherRepository.save(teacher);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Teacher " + teacherId + " does not teach subject " + subjectId);
        }
    }

    @Transactional(readOnly = true)
    public List<TeacherDisplayDto> getTeachersBySubjectName(String subjectName) {
        List<Teacher> teachers = teacherRepository.findTeachersBySubjectName(subjectName);
        return teachers.stream()
                .map(teacherMapper::toDto)
                .toList();
    }
}