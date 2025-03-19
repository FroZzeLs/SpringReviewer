package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.dao.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Отзыв не найден"));
    }

    public List<Review> getReviewsByTeacher(Long teacherId) {
        return reviewRepository.findByTeacherId(teacherId);
    }

    public Review createReview(Review review) {
        return reviewRepository.save(review);
    }

    public Review updateReview(Long id, Review updatedReview) {
        Review existingReview = getReviewById(id);
        existingReview.setComment(updatedReview.getComment());
        existingReview.setGrade(updatedReview.getGrade());
        existingReview.setDate(updatedReview.getDate());
        return reviewRepository.save(existingReview);
    }

    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        reviewRepository.delete(review);
    }
}
