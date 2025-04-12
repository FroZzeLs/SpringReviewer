package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDisplayDto createReview(@Valid @RequestBody ReviewCreateDto reviewCreateDto) {
        return reviewService.saveReview(reviewCreateDto);
    }

    @GetMapping
    public List<ReviewDisplayDto> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public ReviewDisplayDto getReviewById(@PathVariable @Min(value = 1,
            message = "Review ID must be positive") Integer id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping("/username/{username}")
    public List<ReviewDisplayDto> getReviewsByUsername(@PathVariable @NotBlank(message
            = "Username cannot be blank") String username) {
        return reviewService.getReviewsByUserUsername(username);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable @Min(value = 1,
            message = "Review ID must be positive") Integer id) {
        reviewService.deleteReview(id);
    }

    @PutMapping("/{id}")
    public ReviewDisplayDto updateReview(
            @PathVariable @Min(value = 1, message = "Review ID must be positive") Integer id,
                                         @Valid @RequestBody ReviewCreateDto reviewCreateDto) {
        return reviewService.updateReview(id, reviewCreateDto);
    }

    @GetMapping("/teacher/{teacherId}")
    public List<ReviewDisplayDto> getReviewsByTeacherId(@PathVariable @Min(value = 1,
            message = "Teacher ID must be positive") Integer teacherId) {
        return reviewService.getReviewsByTeacherId(teacherId);
    }

    @GetMapping("/user/{userId}")
    public List<ReviewDisplayDto> getReviewsByUserId(@PathVariable @Min(value = 1,
            message = "User ID must be positive") Integer userId) {
        return reviewService.getReviewsByUserId(userId);
    }

    @GetMapping("/stats/teacher-counts")
    public List<Object[]> getReviewCountsPerTeacher() {
        return reviewService.getReviewCountsPerTeacher();
    }

    @GetMapping("/search")
    public List<ReviewDisplayDto> searchReviews(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String teacherSurname,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) @Min(value = 1,
                    message = "Minimum grade must be at least 1") Integer minGrade) {
        return reviewService.searchReviews(startDate,
                endDate, teacherSurname, subjectName, minGrade);
    }
}