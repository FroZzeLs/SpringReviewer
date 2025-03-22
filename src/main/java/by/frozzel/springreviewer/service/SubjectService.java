package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.mapper.SubjectMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.ReviewRepository;
import by.frozzel.springreviewer.repository.SubjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import by.frozzel.springreviewer.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;
    private final ReviewRepository reviewRepository;
    private final TeacherRepository teacherRepository;

    @Transactional
    public SubjectDisplayDto createSubject(SubjectCreateDto dto) {
        Subject subject = subjectMapper.toEntity(dto);
        return subjectMapper.toDto(subjectRepository.save(subject));
    }

    public List<SubjectDisplayDto> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<SubjectDisplayDto> getSubjectById(Integer id) {
        return Optional.ofNullable(subjectRepository.findById(id).map(subjectMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "Subjects not found for id: " + id)));
    }

    public Optional<SubjectDisplayDto> getSubjectByName(String name) {
        return subjectRepository.findByName(name).map(subjectMapper::toDto);
    }

    @Transactional
    public Optional<SubjectDisplayDto> updateSubject(Integer id, SubjectCreateDto dto) {
        return subjectRepository.findById(id)
                .map(existingSubject -> {
                    existingSubject.setName(dto.getName());
                    return subjectMapper.toDto(subjectRepository.save(existingSubject));
                });
    }

    @Transactional
    public ResponseEntity<Void> deleteSubject(int subjectId) {
        if (subjectId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject ID must be a positive number");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        for (Teacher teacher : subject.getTeachers()) {
            teacher.getSubjects().remove(subject);
            teacherRepository.save(teacher);
        }
        
        reviewRepository.deleteBySubjectId(subjectId);

        subjectRepository.delete(subject);
        return null;
    }
}
