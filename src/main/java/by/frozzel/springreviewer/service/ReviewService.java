package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.mapper.ReviewMapper;
import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.ReviewRepository;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import by.frozzel.springreviewer.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewDisplayDto saveReview(ReviewCreateDto dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "User not found"));

        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "Teacher not found"));

        subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "Subject not found"));

        boolean isTeaching = teacher.getSubjects().stream()
                .anyMatch(s -> s.getId() == dto.getSubjectId());

        if (!isTeaching) {
            throw new ResponseStatusException(HttpStatus
                    .BAD_REQUEST, "The teacher does not teach this subject");
        }

        Review review = reviewMapper.toEntity(dto);

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    public List<ReviewDisplayDto> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public ReviewDisplayDto getReviewById(Integer id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reviews not found with id: " + id));
    }

    public void deleteReview(Integer id) {
        reviewRepository.deleteById(id);
    }

    public ReviewDisplayDto updateReview(Integer id, ReviewCreateDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reviews not found with id: " + id));

        review.setDate(dto.getDate());
        review.setGrade(dto.getGrade());
        review.setComment(dto.getComment());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    public List<ReviewDisplayDto> getReviewsByTeacherId(Integer teacherId) {
        List<Review> reviews = reviewRepository.findByTeacherId(teacherId);

        if (reviews.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reviews not found for teacherId: " + teacherId);
        }

        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public List<ReviewDisplayDto> getReviewsByUserId(Integer userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);

        if (reviews.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reviews not found for userId: " + userId);
        }

        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    public List<ReviewDisplayDto> getReviewsByUserUsername(String username) {
        List<Review> reviews = reviewRepository.findByUserUsername(username);

        if (reviews.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reviews not found for user: " + username);
        }

        return reviews.stream()
                .map(reviewMapper::toDto)
                .toList();
    }
}
