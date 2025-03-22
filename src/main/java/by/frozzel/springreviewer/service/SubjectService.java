package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.mapper.SubjectMapper;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.repository.SubjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

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
        return subjectRepository.findById(id).map(subjectMapper::toDto);
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
    public boolean deleteSubject(Integer id) {
        if (subjectRepository.existsById(id)) {
            subjectRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
