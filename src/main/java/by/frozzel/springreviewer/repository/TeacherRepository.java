package by.frozzel.springreviewer.repository;

import by.frozzel.springreviewer.model.Teacher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findBySurnameAndName(String surname, String name);
}
