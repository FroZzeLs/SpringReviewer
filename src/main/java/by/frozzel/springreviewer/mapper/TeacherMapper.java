package by.frozzel.springreviewer.mapper;

import by.frozzel.springreviewer.dto.TeacherCreateDto;
import by.frozzel.springreviewer.dto.TeacherDisplayDto;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class TeacherMapper {

    public Teacher toEntity(TeacherCreateDto dto) {
        Teacher teacher = new Teacher();
        teacher.setSurname(dto.getSurname());
        teacher.setName(dto.getName());
        teacher.setPatronym(dto.getPatronym());
        teacher.setSubjects(new ArrayList<>()); // 🛠 Инициализируем пустым списком
        return teacher;
    }

    public TeacherDisplayDto toDto(Teacher teacher) {
        return TeacherDisplayDto.builder()
                .id(teacher.getId())
                .surname(teacher.getSurname())
                .name(teacher.getName())
                .patronym(teacher.getPatronym())
                .subjects(teacher.getSubjects() != null
                        ? teacher.getSubjects().stream()
                        .map(Subject::getName)
                        .collect(Collectors.toList())
                        : new ArrayList<>()) // 🛠 Проверяем `null`, создаем пустой список
                .build();
    }
}

