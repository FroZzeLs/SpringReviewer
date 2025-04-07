package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.config.LruCache;
import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.mapper.ReviewMapper;
import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.model.User;
import by.frozzel.springreviewer.repository.ReviewRepository;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import by.frozzel.springreviewer.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
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

    private static final String KEY_PREFIX_REVIEW_BY_ID = "reviewById";
    private static final String KEY_PREFIX_REVIEWS_BY_TEACHER = "reviewsByTeacherId";
    private static final String KEY_PREFIX_REVIEWS_BY_USER_ID = "reviewsByUserId";
    private static final String KEY_PREFIX_REVIEWS_BY_USERNAME = "reviewsByUsername";
    private static final String KEY_PREFIX_SEARCH_REVIEWS = "searchReviews";
    private static final String KEY_ALL_REVIEWS = "allReviews";
    private static final String KEY_REVIEW_COUNTS = "reviewCountsPerTeacherNative";
    private static final String KEY_PART_NULL = "null";
    private static final String KEY_SEPARATOR = ":";

    private static final String LOG_CACHE_HIT = "Cache HIT for key: {}";
    private static final String LOG_CACHE_MISS = "Cache MISS for key: {}";
    private static final String LOG_CACHE_PUT = "Put into cache entry for key: {}";
    private static final String LOG_CACHE_REMOVE = "Removed cache entry for key: {}";
    private static final String LOG_CACHE_INVALIDATE = "Invalidated cache entry for key: {}";
    private static final String LOG_FETCH_DB = "Fetching data from repository for key: {}";
    private static final String LOG_REVIEW_NOT_FOUND = "Review not found with id: {}";

    private String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object param : params) {
            keyBuilder.append(KEY_SEPARATOR);
            keyBuilder.append(param == null ? KEY_PART_NULL : param.toString());
        }
        return keyBuilder.toString();
    }

    private void invalidateListCaches(Review review) {
        if (review == null) {
            return;
        }

        lruCache.remove(KEY_ALL_REVIEWS);
        log.debug(LOG_CACHE_INVALIDATE, KEY_ALL_REVIEWS);

        lruCache.remove(KEY_REVIEW_COUNTS);
        log.debug(LOG_CACHE_INVALIDATE, KEY_REVIEW_COUNTS);

        Teacher teacher = review.getTeacher();
        User user = review.getUser();

        if (teacher != null) {
            String teacherCacheKey = generateCacheKey(KEY_PREFIX_REVIEWS_BY_TEACHER,
                    teacher.getId());
            lruCache.remove(teacherCacheKey);
            log.debug(LOG_CACHE_INVALIDATE, teacherCacheKey);
        }

        if (user != null) {
            String userCacheKey = generateCacheKey(KEY_PREFIX_REVIEWS_BY_USER_ID,
                    user.getId());
            lruCache.remove(userCacheKey);
            log.debug(LOG_CACHE_INVALIDATE, userCacheKey);

            if (user.getUsername() != null) {
                String usernameCacheKey = generateCacheKey(KEY_PREFIX_REVIEWS_BY_USERNAME,
                        user.getUsername());
                lruCache.remove(usernameCacheKey);
                log.debug(LOG_CACHE_INVALIDATE, usernameCacheKey);
            }
        }
    }

    @Transactional
    public ReviewDisplayDto saveReview(ReviewCreateDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "User not found"));
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "Teacher not found"));

        Integer subjectIdFromDto = dto.getSubjectId();
        if (subjectIdFromDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Subject ID in DTO cannot be null");
        }

        boolean isTeaching = teacher.getSubjects().stream()
                .anyMatch(s -> s.getId() == subjectIdFromDto);

        if (!isTeaching) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "The teacher does not teach this subject");
        }

        Subject subject = subjectRepository.findById(subjectIdFromDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus
                        .NOT_FOUND, "Subject not found"));

        Review review = reviewMapper.toEntity(dto);
        review.setUser(user);
        review.setTeacher(teacher);
        review.setSubject(subject);
        review.setDate(Objects.requireNonNullElseGet(dto.getDate(), LocalDate::now));

        Review savedReview = reviewRepository.save(review);
        ReviewDisplayDto savedDto = reviewMapper.toDto(savedReview);

        invalidateListCaches(savedReview);

        String specificReviewKey = generateCacheKey(KEY_PREFIX_REVIEW_BY_ID, savedReview.getId());
        lruCache.put(specificReviewKey, savedDto);
        log.debug(LOG_CACHE_PUT, specificReviewKey);

        return savedDto;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getAllReviews() {
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache
                .get(KEY_ALL_REVIEWS);

        if (cachedReviews != null) {
            log.debug(LOG_CACHE_HIT, KEY_ALL_REVIEWS);
            return cachedReviews;
        } else {
            log.debug(LOG_CACHE_MISS, KEY_ALL_REVIEWS);
            log.debug(LOG_FETCH_DB, KEY_ALL_REVIEWS);
            List<ReviewDisplayDto> reviews = reviewRepository.findAll()
                    .stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(KEY_ALL_REVIEWS, reviews);
            log.debug(LOG_CACHE_PUT, KEY_ALL_REVIEWS);
            return reviews;
        }
    }

    @Transactional(readOnly = true)
    public ReviewDisplayDto getReviewById(Integer id) {
        String cacheKey = generateCacheKey(KEY_PREFIX_REVIEW_BY_ID, id);
        ReviewDisplayDto cachedReview = (ReviewDisplayDto) lruCache.get(cacheKey);

        if (cachedReview != null) {
            log.debug(LOG_CACHE_HIT, cacheKey);
            return cachedReview;
        } else {
            log.debug(LOG_CACHE_MISS, cacheKey);
            log.debug(LOG_FETCH_DB, cacheKey);
            ReviewDisplayDto reviewDto = reviewRepository.findById(id)
                    .map(reviewMapper::toDto)
                    .orElseThrow(() -> {
                        log.warn(LOG_REVIEW_NOT_FOUND, id);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Review not found with id: " + id);
                    });
            lruCache.put(cacheKey, reviewDto);
            log.debug(LOG_CACHE_PUT, cacheKey);
            return reviewDto;
        }
    }

    @Transactional
    public void deleteReview(Integer id) {
        Review reviewToDelete = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(LOG_REVIEW_NOT_FOUND, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Review not found with id: " + id);
                });

        reviewRepository.deleteById(id);
        log.debug("Deleted review with id: {}", id);

        invalidateListCaches(reviewToDelete);

        String specificReviewKey = generateCacheKey(KEY_PREFIX_REVIEW_BY_ID, id);
        lruCache.remove(specificReviewKey);
        log.debug(LOG_CACHE_REMOVE, specificReviewKey);
    }

    @Transactional
    public ReviewDisplayDto updateReview(Integer id, ReviewCreateDto dto) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(LOG_REVIEW_NOT_FOUND, id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Review not found with id: " + id);
                });

        boolean updated = false;
        if (dto.getDate() != null && !dto.getDate().equals(review.getDate())) {
            review.setDate(dto.getDate());
            updated = true;
        }
        if (dto.getGrade() != null && !dto.getGrade().equals(review.getGrade())) {
            review.setGrade(dto.getGrade());
            updated = true;
        }
        if (dto.getComment() != null && !dto.getComment().equals(review.getComment())) {
            review.setComment(dto.getComment());
            updated = true;
        }

        if (updated) {
            Review updatedReview = reviewRepository.save(review);
            ReviewDisplayDto updatedDto = reviewMapper.toDto(updatedReview);

            invalidateListCaches(updatedReview);

            String specificReviewKey = generateCacheKey(KEY_PREFIX_REVIEW_BY_ID,
                    updatedReview.getId());
            lruCache.put(specificReviewKey, updatedDto);
            log.debug(LOG_CACHE_PUT, specificReviewKey);
            return updatedDto;
        } else {
            log.debug("Review with id {} was not modified.", id);
            return getReviewById(id);
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getReviewsByTeacherId(Integer teacherId) {
        String cacheKey = generateCacheKey(KEY_PREFIX_REVIEWS_BY_TEACHER, teacherId);
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            log.debug(LOG_CACHE_HIT, cacheKey);
            return cachedReviews;
        } else {
            log.debug(LOG_CACHE_MISS, cacheKey);
            log.debug(LOG_FETCH_DB, cacheKey);
            List<Review> reviews = reviewRepository.findByTeacherId(teacherId);
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            log.debug(LOG_CACHE_PUT, cacheKey);
            return reviewDtos;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getReviewsByUserId(Integer userId) {
        String cacheKey = generateCacheKey(KEY_PREFIX_REVIEWS_BY_USER_ID, userId);
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            log.debug(LOG_CACHE_HIT, cacheKey);
            return cachedReviews;
        } else {
            log.debug(LOG_CACHE_MISS, cacheKey);
            log.debug(LOG_FETCH_DB, cacheKey);
            List<Review> reviews = reviewRepository.findByUserId(userId);
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            log.debug(LOG_CACHE_PUT, cacheKey);
            return reviewDtos;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> getReviewsByUserUsername(String username) {
        String cacheKey = generateCacheKey(KEY_PREFIX_REVIEWS_BY_USERNAME, username);
        List<ReviewDisplayDto> cachedReviews = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedReviews != null) {
            log.debug(LOG_CACHE_HIT, cacheKey);
            return cachedReviews;
        } else {
            log.debug(LOG_CACHE_MISS, cacheKey);
            log.debug(LOG_FETCH_DB, cacheKey);
            List<Review> reviews = reviewRepository.findByUserUsername(username);
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            log.debug(LOG_CACHE_PUT, cacheKey);
            return reviewDtos;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Object[]> getReviewCountsPerTeacher() {
        List<Object[]> cachedCounts = (List<Object[]>) lruCache.get(KEY_REVIEW_COUNTS);
        if (cachedCounts != null) {
            log.debug(LOG_CACHE_HIT, KEY_REVIEW_COUNTS);
            return cachedCounts;
        } else {
            log.debug(LOG_CACHE_MISS, KEY_REVIEW_COUNTS);
            log.debug(LOG_FETCH_DB, KEY_REVIEW_COUNTS);
            List<Object[]> counts = reviewRepository.countReviewsPerTeacher();
            lruCache.put(KEY_REVIEW_COUNTS, counts);
            log.debug(LOG_CACHE_PUT, KEY_REVIEW_COUNTS);
            return counts;
        }
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<ReviewDisplayDto> searchReviews(LocalDate startDate,
                                                LocalDate endDate, String teacherSurname,
                                                String subjectName, Integer minGrade) {

        String cacheKey = generateCacheKey(KEY_PREFIX_SEARCH_REVIEWS,
                startDate, endDate, teacherSurname, subjectName, minGrade);
        log.info("Searching reviews (cache check) for key: {}", cacheKey);

        List<ReviewDisplayDto> cachedResult = (List<ReviewDisplayDto>) lruCache.get(cacheKey);

        if (cachedResult != null) {
            log.debug(LOG_CACHE_HIT, cacheKey);
            return cachedResult;
        } else {
            log.debug(LOG_CACHE_MISS, cacheKey);
            log.info("Searching reviews with criteria: startDate={}, endDate={},"
                           + " teacherSurname='{}', subjectName='{}', minGrade={}",
                    startDate, endDate, teacherSurname, subjectName, minGrade);
            List<Review> reviews = reviewRepository.searchReviews(startDate, endDate,
                    teacherSurname, subjectName, minGrade);
            List<ReviewDisplayDto> reviewDtos = reviews.stream()
                    .map(reviewMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, reviewDtos);
            log.debug(LOG_CACHE_PUT, cacheKey);
            return reviewDtos;
        }
    }
}