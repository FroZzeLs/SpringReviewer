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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found"));
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Teacher not found"));
        subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Subject not found"));

        boolean isTeaching = teacher.getSubjects().stream()
                .anyMatch(s -> s.getId() == dto.getSubjectId());
        if (!isTeaching) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The teacher does not teach this subject");
        }

        Review review = reviewMapper.toEntity(dto);
        if (review.getDate() == null) {
            review.setDate(LocalDate.now());
        }
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
            log.debug("Cache HIT for key: {}", cacheKey);
            return cachedReviews;
        } else {
            log.debug("Cache MISS for key: {}", cacheKey);
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
            log.debug("Cache HIT for key: {}", cacheKey);
            return cachedReview;
        } else {
            log.debug("Cache MISS for key: {}", cacheKey);
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
            lruCache.clear(); // Очищаем кэш
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

        if (dto.getDate() != null) {
            review.setDate(dto.getDate());
        }
        if (dto.getGrade() != null) {
            review.setGrade(dto.getGrade());
        }
        if (dto.getComment() != null) {
            review.setComment(dto.getComment());
        }

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
            log.debug("Cache HIT for key: {}", cacheKey);
            return cachedReviews;
        } else {
            log.debug("Cache MISS for key: {}", cacheKey);
            List<Review> reviews = reviewRepository.findByTeacherId(teacherId);
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
            log.debug("Cache HIT for key: {}", cacheKey);
            return cachedReviews;
        } else {
            log.debug("Cache MISS for key: {}", cacheKey);
            List<Review> reviews = reviewRepository.findByUserId(userId);
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
            log.debug("Cache HIT for key: {}", cacheKey);
            return cachedReviews;
        } else {
            log.debug("Cache MISS for key: {}", cacheKey);
            List<Review> reviews = reviewRepository.findByUserUsername(username);
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
        if (cachedCounts != null) {
            log.debug("Cache HIT for key: {}", cacheKey);
            return cachedCounts;
        } else {
            log.debug("Cache MISS for key: {}", cacheKey);
            List<Object[]> counts = reviewRepository.countReviewsPerTeacherNative();
            lruCache.put(cacheKey, counts);
            return counts;
        }
    }

    @Transactional(readOnly = true)
    public List<ReviewDisplayDto> searchReviews(LocalDate startDate,
                                                LocalDate endDate, String teacherSurname,
                                                String subjectName, Integer minGrade) {
        log.info("Searching reviews with criteria: startDate={}, endDate={},"
                       + " teacherSurname='{}' (type: {}), subjectName='{}'"
                       + " (type: {}), minGrade={}",
                startDate, endDate,
                teacherSurname, (teacherSurname != null
                        ? teacherSurname.getClass().getName() : "null"),
                subjectName, (subjectName != null ? subjectName.getClass().getName() : "null"),
                minGrade);

        List<Review> reviews = reviewRepository.searchReviews(startDate, endDate,
                teacherSurname, subjectName, minGrade);
        return reviews.stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
}