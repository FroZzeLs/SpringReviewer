package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.service.ReviewService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDisplayDto createReview(@RequestBody ReviewCreateDto reviewCreateDto) {
        return reviewService.saveReview(reviewCreateDto);
    }

    @GetMapping
    public List<ReviewDisplayDto> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ReviewDisplayDto getReviewById(@PathVariable Integer id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping("/username/{username}")
    public List<ReviewDisplayDto> getReviewsByUsername(@PathVariable String username) {
        return reviewService.getReviewsByUserUsername(username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable Integer id) {
        reviewService.deleteReview(id);
    }

    @PutMapping("/{id}")
    public ReviewDisplayDto updateReview(@PathVariable Integer id,
                                         @RequestBody ReviewCreateDto reviewCreateDto) {
        return reviewService.updateReview(id, reviewCreateDto);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<ReviewDisplayDto> getReviewsByTeacherId(@PathVariable Integer teacherId) {
        return reviewService.getReviewsByTeacherId(teacherId);
    }

    @GetMapping("/user/{userId}")
    public List<ReviewDisplayDto> getReviewsByUserId(@PathVariable Integer userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    @GetMapping("/stats/teacher-counts")
    public List<Object[]> getReviewCountsPerTeacher() {
        return reviewService.getReviewCountsPerTeacher();
    }
}