package by.frozzel.springreviewer.repository;

import by.frozzel.springreviewer.model.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByTeacherId(Integer teacherId);

    List<Review> findByUserId(Integer userId);
}
