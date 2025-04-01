package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.config.LruCache;
import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.mapper.TeacherMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import java.util.List;
import java.util.stream.Collectors;
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
    private final LruCache<String, Object> lruCache;
    private static final String TEACHER_NOT_FOUND_MESSAGE = "Teacher not found with ID: ";
    private static final String TEACHER_BY_NAME_NOT_FOUND_MESSAGE = "Teacher not found with surname: %s and name: %s";
    private static final String SUBJECT_NOT_FOUND_MESSAGE = "Subject not found with ID: ";

    private String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object param : params) {
            keyBuilder.append(":");
            keyBuilder.append(param == null ? "null" : param.toString());
        }
        return keyBuilder.toString();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TeacherDisplayDto> getAllTeachers() {
        String cacheKey = generateCacheKey("allTeachers");
        List<TeacherDisplayDto> cachedTeachers = (List<TeacherDisplayDto>) lruCache.get(cacheKey);
        if (cachedTeachers != null) {
            return cachedTeachers;
        } else {
            List<TeacherDisplayDto> teachers = teacherRepository.findAll().stream()
                    .map(teacherMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, teachers);
            return teachers;
        }
    }

    @Transactional(readOnly = true)
    public TeacherDisplayDto getTeacherById(Integer id) {
        String cacheKey = generateCacheKey("teacherById", id);
        TeacherDisplayDto cachedTeacher = (TeacherDisplayDto) lruCache.get(cacheKey);
        if (cachedTeacher != null) {
            return cachedTeacher;
        } else {
            Teacher teacher = teacherRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TEACHER_NOT_FOUND_MESSAGE + id));
            TeacherDisplayDto dto = teacherMapper.toDto(teacher);
            lruCache.put(cacheKey, dto);
            return dto;
        }
    }

    @Transactional
    public TeacherDisplayDto createTeacher(TeacherCreateDto teacherCreateDto) {
        Teacher teacher = teacherMapper.toEntity(teacherCreateDto);
        Teacher savedTeacher = teacherRepository.save(teacher);
        lruCache.clear();
        return teacherMapper.toDto(savedTeacher);
    }

    @Transactional
    public TeacherDisplayDto updateTeacher(Integer id, TeacherCreateDto teacherCreateDto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, TEACHER_NOT_FOUND_MESSAGE + id));
        teacher.setSurname(teacherCreateDto.getSurname());
        teacher.setName(teacherCreateDto.getName());
        teacher.setPatronym(teacherCreateDto.getPatronym());
        Teacher updatedTeacher = teacherRepository.save(teacher);
        lruCache.clear();
        return teacherMapper.toDto(updatedTeacher);
    }

    @Transactional
    public void deleteTeacher(Integer id) {
        if (teacherRepository.existsById(id)) {
            teacherRepository.deleteById(id);
            lruCache.clear();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, TEACHER_NOT_FOUND_MESSAGE + id);
        }
    }

    @Transactional(readOnly = true)
    public TeacherDisplayDto getTeacherByFullName(String surname, String name) {
        String cacheKey = generateCacheKey("teacherByName", surname, name);
        TeacherDisplayDto cachedTeacher = (TeacherDisplayDto) lruCache.get(cacheKey);
        if(cachedTeacher != null) {
            return cachedTeacher;
        } else {
            Teacher teacher = teacherRepository.findBySurnameAndName(surname, name)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            String.format(TEACHER_BY_NAME_NOT_FOUND_MESSAGE, surname, name)));
            TeacherDisplayDto dto = teacherMapper.toDto(teacher);
            lruCache.put(cacheKey, dto);
            return dto;
        }
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
            lruCache.clear();
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
            lruCache.clear();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Teacher " + teacherId + " does not teach subject " + subjectId);
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TeacherDisplayDto> getTeachersBySubjectName(String subjectName) {
        String cacheKey = generateCacheKey("teachersBySubjectName", subjectName);
        List<TeacherDisplayDto> cachedTeachers = (List<TeacherDisplayDto>) lruCache.get(cacheKey);
        if (cachedTeachers != null) {
            return cachedTeachers;
        } else {
            List<Teacher> teachers = teacherRepository.findTeachersBySubjectName(subjectName);
            List<TeacherDisplayDto> dtos = teachers.stream()
                    .map(teacherMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, dtos);
            return dtos;
        }
    }
}