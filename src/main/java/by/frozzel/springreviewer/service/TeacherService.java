package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.model.Teacher;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {

    public List<Teacher> getTeachersBySubject(String subject) {
        return Arrays.asList(
                Teacher.builder().surname("Скиба").name("Ирина")
                        .patronym("Геннадьевна").subject(subject).build(),
                Teacher.builder().surname("Луцик").name("Юрий")
                        .patronym("Александрович").subject(subject).build()
        );
    }
}