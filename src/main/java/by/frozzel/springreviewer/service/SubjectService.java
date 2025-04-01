package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.config.LruCache;
import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.mapper.SubjectMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.ReviewRepository;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import java.util.List;
import java.util.Optional;
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
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final ReviewRepository reviewRepository;
    private final TeacherRepository teacherRepository;
    private final LruCache<String, Object> lruCache;

    private String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object param : params) {
            keyBuilder.append(":");
            keyBuilder.append(param == null ? "null" : param.toString());
        }
        return keyBuilder.toString();
    }

    @Transactional
    public SubjectDisplayDto createSubject(SubjectCreateDto dto) {
        Subject subject = subjectMapper.toEntity(dto);
        Subject savedSubject = subjectRepository.save(subject);
        lruCache.clear();
        return subjectMapper.toDto(savedSubject);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<SubjectDisplayDto> getAllSubjects() {
        String cacheKey = generateCacheKey("allSubjects");
        List<SubjectDisplayDto> cachedSubjects = (List<SubjectDisplayDto>) lruCache.get(cacheKey);
        if (cachedSubjects != null) {
            return cachedSubjects;
        } else {
            List<SubjectDisplayDto> subjects = subjectRepository.findAll().stream()
                    .map(subjectMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, subjects);
            return subjects;
        }
    }

    @Transactional(readOnly = true)
    public Optional<SubjectDisplayDto> getSubjectById(Integer id) {
        String cacheKey = generateCacheKey("subjectById", id);
        SubjectDisplayDto cachedSubject = (SubjectDisplayDto) lruCache.get(cacheKey);
        if (cachedSubject != null) {
            return Optional.of(cachedSubject);
        } else {
            Optional<SubjectDisplayDto> subjectOpt = subjectRepository.findById(id)
                    .map(subjectMapper::toDto);
            subjectOpt.ifPresent(dto -> lruCache.put(cacheKey, dto));
            return subjectOpt;
        }
    }

    @Transactional(readOnly = true)
    public Optional<SubjectDisplayDto> getSubjectByName(String name) {
        String cacheKey = generateCacheKey("subjectByName", name);
        SubjectDisplayDto cachedSubject = (SubjectDisplayDto) lruCache.get(cacheKey);
        if (cachedSubject != null) {
            return Optional.of(cachedSubject);
        } else {
            Optional<SubjectDisplayDto> subjectOpt = subjectRepository.findByName(name)
                    .map(subjectMapper::toDto);
            subjectOpt.ifPresent(dto -> lruCache.put(cacheKey, dto));
            return subjectOpt;
        }
    }

    @Transactional
    public Optional<SubjectDisplayDto> updateSubject(Integer id, SubjectCreateDto dto) {
        return subjectRepository.findById(id)
                .map(existingSubject -> {
                    existingSubject.setName(dto.getName());
                    Subject updatedSubject = subjectRepository.save(existingSubject);
                    lruCache.clear();
                    return Optional.of(subjectMapper.toDto(updatedSubject));
                })
                .orElse(Optional.empty());
    }

    @Transactional
    public void deleteSubject(int subjectId) {
        if (subjectId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Subject ID must be a positive number");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Subject not found with id: " + subjectId));

        List<Teacher> teachersToRemoveFrom = List.copyOf(subject.getTeachers());
        for (Teacher teacher : teachersToRemoveFrom) {
            teacher.getSubjects().remove(subject);
            teacherRepository.save(teacher);
        }
        subject.getTeachers().clear();

        reviewRepository.deleteBySubjectId(subjectId);
        subjectRepository.delete(subject);
        lruCache.clear();
    }
}