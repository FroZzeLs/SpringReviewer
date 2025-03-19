package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.dao.SubjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public Subject getSubjectByName(String name) {
        return subjectRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Предмет с названием '" + name + "' не найден!"));
    }

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public Subject updateSubject(String name, Subject updatedSubject) {
        Subject existingSubject = getSubjectByName(name);
        existingSubject.setName(updatedSubject.getName());
        return subjectRepository.save(existingSubject);
    }

    public void deleteSubject(String name) {
        Subject subject = getSubjectByName(name);
        subjectRepository.delete(subject);
    }
}
