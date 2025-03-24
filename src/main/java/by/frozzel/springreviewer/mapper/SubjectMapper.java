package by.frozzel.springreviewer.mapper;

import by.frozzel.springreviewer.dto.SubjectCreateDto;
import by.frozzel.springreviewer.dto.SubjectDisplayDto;
import by.frozzel.springreviewer.model.Subject;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {
    public Subject toEntity(SubjectCreateDto dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setTeachers(Collections.emptyList());
        return subject;
    }

    public SubjectDisplayDto toDto(Subject subject) {
        return new SubjectDisplayDto(
                subject.getId(),
                subject.getName(),
                subject.getTeachers() != null
                        ? subject.getTeachers().stream()
                        .map(teacher -> teacher.getSurname() + " " + teacher.getName() + " "
                                + teacher.getPatronym())
                        .collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }
}
