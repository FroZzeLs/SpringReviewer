package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.model.Teacher;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    public List<Teacher> getTeachersBySubject(String subject) {
        return Arrays.asList(
                Teacher.builder().id(1).name("Лукьянова Ирина Викторовна").subject(subject).build(),
                Teacher.builder().id(2).name("Луцик Юрий Александрович").subject(subject).build()
        );
    }
}