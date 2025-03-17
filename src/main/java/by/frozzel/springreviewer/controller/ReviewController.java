package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.service.ReviewService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<Review> getReviewsByTeacher(@RequestParam int teacherId) {
        return reviewService.getReviewsByTeacher(teacherId);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return reviewService.getReviewById(id); 
    }

    @GetMapping("/all")
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }
}
