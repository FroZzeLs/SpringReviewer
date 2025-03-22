package by.frozzel.springreviewer.repository;

import by.frozzel.springreviewer.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByTeacherId(Integer teacherId);
    List<Review> findByUserId(Integer userId);
}
