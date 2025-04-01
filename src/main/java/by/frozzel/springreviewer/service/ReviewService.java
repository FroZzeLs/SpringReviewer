package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.config.LruCache;
import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.mapper.ReviewMapper;
import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.repository.ReviewRepository;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import by.frozzel.springreviewer.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final LruCache<String, Object> lruCache;

    private String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object param : params) {
            keyBuilder.append(":");
            keyBuilder.append(param == null ? "null" : param.toString());
        }
        return keyBuilder.toString();
    }

    @Transactional
    public ReviewDisplayDto saveReview(ReviewCreateDto dto) {
        userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found"));
        subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));

        boolean isTeaching = teacher.getSubjects().stream()
                .anyMatch(s -> s.getId() == dto.getSubjectId());
        if (!isTeaching) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The teacher does not teach this subject");
        }

        Review review = reviewMapper.toEntity(dto);
        Review savedReview = reviewRepository.save(review);

        lruCache.clear();
        return reviewMapper.toDto(savedReview);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getAllReviews() {
        String cacheKey = generateCacheKey("allReviews");
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            return cachedReviews;
        } else {
            List<ReviewDisplayDto> reviews = reviewRepository.findAll()
                    .stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviews);
            return reviews;
        }
    }

    @Transactional(readOnly = true)
    public ReviewDisplayDto getReviewById(Integer id) {
        String cacheKey = generateCacheKey("reviewById", id);
        ReviewDisplayDto cachedReview = (ReviewDisplayDto) lruCache.get(cacheKey);

        if (cachedReview != null) {
            return cachedReview;
        } else {
            ReviewDisplayDto reviewDto = reviewRepository.findById(id)
                    .map(reviewMapper::toDto)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Review not found with id: " + id));
            lruCache.put(cacheKey, reviewDto);
            return reviewDto;
        }
    }

    @Transactional
    public void deleteReview(Integer id) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            lruCache.clear();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Review not found with id: " + id);
        }
    }

    @Transactional
    public ReviewDisplayDto updateReview(Integer id, ReviewCreateDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Review not found with id: " + id));

        review.setDate(dto.getDate());
        review.setGrade(dto.getGrade());
        review.setComment(dto.getComment());

        Review updatedReview = reviewRepository.save(review);
        lruCache.clear();
        return reviewMapper.toDto(updatedReview);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getReviewsByTeacherId(Integer teacherId) {
        String cacheKey = generateCacheKey("reviewsByTeacherId", teacherId);
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            return cachedReviews;
        } else {
            List<Review> reviews = reviewRepository.findByTeacherId(teacherId);
            if (reviews.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reviews not found for teacherId: " + teacherId);
            }
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            return reviewDtos;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getReviewsByUserId(Integer userId) {
        String cacheKey = generateCacheKey("reviewsByUserId", userId);
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            return cachedReviews;
        } else {
            List<Review> reviews = reviewRepository.findByUserId(userId);
            if (reviews.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reviews not found for userId: " + userId);
            }
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            return reviewDtos;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getReviewsByUserUsername(String username) {
        String cacheKey = generateCacheKey("reviewsByUsername", username);
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            return cachedReviews;
        } else {
            List<Review> reviews = reviewRepository.findByUserUsername(username);
            if (reviews.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reviews not found for user: " + username);
            }
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            return reviewDtos;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> getReviewCountsPerTeacher() {
        String cacheKey = generateCacheKey("reviewCountsPerTeacherNative");
        List<Object[]> cachedCounts = (List<Object[]>) lruCache.get(cacheKey);
        if(cachedCounts != null) {
            return cachedCounts;
        } else {
            List<Object[]> counts = reviewRepository.countReviewsPerTeacherNative();
            lruCache.put(cacheKey, counts);
            return counts;
        }
    }
}