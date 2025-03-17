package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dao.ReviewDao;
import by.frozzel.springreviewer.model.Review;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewService {
    private final ReviewDao reviewDao;

    public ReviewService(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    public List<Review> getReviewsByTeacher(int teacherId) {
        return reviewDao.findByTeacherId(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Отзывы для данного преподавателя не найдены"));
    }

    public Review getReviewById(int id) {
        return reviewDao.findById(id)
                .orElseThrow(() -> new
                        ResponseStatusException(HttpStatus.NOT_FOUND, "Отзыв не найден"));
    }

    public List<Review> getAllReviews() {
        return reviewDao.findAll();
    }
}
