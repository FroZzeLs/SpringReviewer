package by.frozzel.springreviewer.dao;

import by.frozzel.springreviewer.model.Review;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class ReviewDao {
    private final List<Review> reviews = List.of(
            new Review(1, 1, "Лучший преподаватель java!"),
            new Review(2, 1, "Весёлый дед, норм поясняет за предмет, помогает с лабами"),
            new Review(3, 3, "Как эта бабка проходит психолога на медкомиссии...")
    );

    public Optional<List<Review>> findByTeacherId(int teacherId) {
        List<Review> result = reviews.stream()
                .filter(review -> review.getTeacherId() == teacherId)
                .toList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }

    public Optional<Review> findById(int id) {
        return reviews.stream()
                .filter(review -> review.getId() == id)
                .findFirst();
    }

    public List<Review> findAll() {
        return reviews;
    }
}
