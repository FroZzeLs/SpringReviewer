package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dao.ReviewDAO;
import by.frozzel.springreviewer.model.Review;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    public List<Review> getReviewsByTeacher(int teacherId) {
        return reviewDAO.findByTeacherId(teacherId);
    }

    public Review getReviewById(int id) {
        return reviewDAO.findById(id);
    }

    public List<Review> getAllReviews() {
        return reviewDAO.findAll();
    }
}
