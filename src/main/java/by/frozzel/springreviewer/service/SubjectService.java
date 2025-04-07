package by.frozzel.springreviewer.service;

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

    @Transactional
    public SubjectDisplayDto createSubject(SubjectCreateDto dto) {
        Subject subject = subjectMapper.toEntity(dto);
        Subject savedSubject = subjectRepository.save(subject);
        return subjectMapper.toDto(savedSubject);
    }

    @Transactional(readOnly = true)
    public List<SubjectDisplayDto> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SubjectDisplayDto> getSubjectById(Integer id) {
        return subjectRepository.findById(id)
                .map(subjectMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<SubjectDisplayDto> getSubjectByName(String name) {
        return subjectRepository.findByName(name)
                .map(subjectMapper::toDto);
    }

    @Transactional
    public Optional<SubjectDisplayDto> updateSubject(Integer id, SubjectCreateDto dto) {
        return subjectRepository.findById(id)
                .map(existingSubject -> {
                    existingSubject.setName(dto.getName());
                    Subject updatedSubject = subjectRepository.save(existingSubject);
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
    }
}