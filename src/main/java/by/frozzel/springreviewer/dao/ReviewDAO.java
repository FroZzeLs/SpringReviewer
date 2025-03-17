package by.frozzel.springreviewer.dao;

import by.frozzel.springreviewer.model.Review;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReviewDAO {
    private final List<Review> reviews = List.of(
            new Review(1, 1, "Лучший преподаватель java!"),
            new Review(2, 1, "Весёлый дед, норм поясняет за предмет, помогает с лабами"),
            new Review(3, 3, "Как эта бабка проходит психолога на медкомиссии...")
    );

    public List<Review> findByTeacherId(int teacherId) {
        return reviews.stream()
                .filter(r -> r.getTeacherId() == teacherId)
                .toList();
    }

    public Review findById(int id) {
        return reviews.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<Review> findAll() {
        return reviews;
    }
}
