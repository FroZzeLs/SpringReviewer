package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.mapper.ReviewMapper;
import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.repository.ReviewRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public ReviewDisplayDto saveReview(ReviewCreateDto reviewCreateDto) {
        Review review = reviewMapper.toEntity(reviewCreateDto);
        return reviewMapper.toDto(reviewRepository.save(review));
    }

    public List<ReviewDisplayDto> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDisplayDto getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));
    }

    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }

    public ReviewDisplayDto updateReview(Integer id, ReviewCreateDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + id));

        review.setDate(dto.getDate());
        review.setGrade(dto.getGrade());
        review.setComment(dto.getComment());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    public List<ReviewDisplayDto> getReviewsByTeacherId(Integer teacherId) {
        return reviewRepository.findByTeacherId(teacherId)
                .stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDisplayDto> getReviewsByUserId(Integer userId) {
        return reviewRepository.findByUserId(userId)
                .stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}
