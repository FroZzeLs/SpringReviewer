package by.frozzel.springreviewer.dao;

import by.frozzel.springreviewer.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    Optional<Teacher> findBySurnameAndName(String surname, String name);
}
