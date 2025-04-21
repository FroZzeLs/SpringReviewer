package by.frozzel.springreviewer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import by.frozzel.springreviewer.config.LruCache;
import by.frozzel.springreviewer.dto.*;
import by.frozzel.springreviewer.exception.BadRequestException;
import by.frozzel.springreviewer.exception.ResourceNotFoundException;
import by.frozzel.springreviewer.mapper.ReviewMapper;
import by.frozzel.springreviewer.model.*;
import by.frozzel.springreviewer.repository.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LruCache<String, Object> lruCache;

    @InjectMocks
    private ReviewService reviewService;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;
    @Captor
    private ArgumentCaptor<String> cacheKeyCaptor;
    @Captor
    private ArgumentCaptor<Object> cacheValueCaptor;


    private User user;
    private Teacher teacher;
    private Subject subject;
    private Review review1;
    private Review review2;
    private ReviewCreateDto reviewCreateDto;
    private ReviewDisplayDto reviewDisplayDto1;
    private ReviewDisplayDto reviewDisplayDto2;
    private TeacherDisplayDto teacherDisplayDto;
    private LocalDate testDate;


    @BeforeEach
    void setUp() {
        testDate = LocalDate.now();

        user = new User(1, "testuser", new ArrayList<>());

        teacher = new Teacher(10, "Ivanov", "Ivan", "I", new ArrayList<>(), new ArrayList<>());
        teacherDisplayDto = TeacherDisplayDto.builder()
                .id(10).surname("Ivanov").name("Ivan").patronym("I").subjects(List.of("Math")).build();

        subject = new Subject(100, "Math", new ArrayList<>());
        teacher.getSubjects().add(subject);


        review1 = new Review(1000, user, teacher, subject, testDate, 5, "Excellent");
        review2 = new Review(1001, user, teacher, subject, testDate.minusDays(1), 4, "Good");

        reviewCreateDto = new ReviewCreateDto();
        reviewCreateDto.setUserId(user.getId());
        reviewCreateDto.setTeacherId(teacher.getId());
        reviewCreateDto.setSubjectId(subject.getId());
        reviewCreateDto.setGrade(5);
        reviewCreateDto.setComment("Excellent");
        reviewCreateDto.setDate(testDate);

        reviewDisplayDto1 = new ReviewDisplayDto(
                review1.getId(), user.getUsername(), teacherDisplayDto, subject.getName(),
                review1.getDate(), review1.getGrade(), review1.getComment()
        );

        reviewDisplayDto2 = new ReviewDisplayDto(
                review2.getId(), user.getUsername(), teacherDisplayDto, subject.getName(),
                review2.getDate(), review2.getGrade(), review2.getComment()
        );
    }

    private String buildKey(String prefix, Object... parts) {
        StringBuilder key = new StringBuilder(prefix);
        for (Object part : parts) {
            key.append(":").append(part == null ? "null" : part.toString());
        }
        return key.toString();
    }

    @Test
    void saveReview_Success_WithDateProvided() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(reviewMapper.toEntity(reviewCreateDto)).thenReturn(review1); // review1 has date set
        when(reviewRepository.save(any(Review.class))).thenReturn(review1);
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);

        ReviewDisplayDto result = reviewService.saveReview(reviewCreateDto);

        assertNotNull(result);
        assertEquals(review1.getDate(), result.getDate());
        verify(reviewRepository).save(reviewCaptor.capture());
        assertEquals(testDate, reviewCaptor.getValue().getDate());

        verify(lruCache, atLeastOnce()).remove(anyString());
        verify(lruCache).put(buildKey("reviewById", review1.getId()), reviewDisplayDto1);
    }

    @Test
    void saveReview_Success_WithDateNull_ShouldUseCurrentDate() {
        reviewCreateDto.setDate(null);
        Review reviewWithNullDate = new Review();
        reviewWithNullDate.setUser(user);
        reviewWithNullDate.setTeacher(teacher);
        reviewWithNullDate.setSubject(subject);
        reviewWithNullDate.setGrade(reviewCreateDto.getGrade());
        reviewWithNullDate.setComment(reviewCreateDto.getComment());
        // Date is null initially

        Review savedReview = new Review(1000, user, teacher, subject, LocalDate.now(), 5, "Excellent");
        ReviewDisplayDto savedReviewDto = new ReviewDisplayDto(1000, user.getUsername(), teacherDisplayDto, subject.getName(), LocalDate.now(), 5, "Excellent");


        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.of(subject));
        when(reviewMapper.toEntity(reviewCreateDto)).thenReturn(reviewWithNullDate);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);
        when(reviewMapper.toDto(savedReview)).thenReturn(savedReviewDto);

        ReviewDisplayDto result = reviewService.saveReview(reviewCreateDto);

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getDate());

        verify(reviewRepository).save(reviewCaptor.capture());
        assertEquals(LocalDate.now(), reviewCaptor.getValue().getDate());

        verify(lruCache, atLeastOnce()).remove(anyString());
        verify(lruCache).put(buildKey("reviewById", savedReview.getId()), savedReviewDto);
    }


    @Test
    void saveReview_TeacherDoesNotTeachSubject() {
        Subject otherSubject = new Subject(999, "History", new ArrayList<>());
        reviewCreateDto.setSubjectId(otherSubject.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(otherSubject.getId())).thenReturn(Optional.of(otherSubject));

        assertThrows(BadRequestException.class, () -> reviewService.saveReview(reviewCreateDto));

        verify(reviewRepository, never()).save(any());
        verify(lruCache, never()).put(anyString(), any());
        verify(lruCache, never()).remove(anyString());
    }

    @Test
    void saveReview_UserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.saveReview(reviewCreateDto));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void saveReview_TeacherNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.saveReview(reviewCreateDto));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void saveReview_SubjectNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(teacherRepository.findById(teacher.getId())).thenReturn(Optional.of(teacher));
        when(subjectRepository.findById(subject.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.saveReview(reviewCreateDto));
        verify(reviewRepository, never()).save(any());
    }


    @Test
    void getAllReviews_CacheHit() {
        List<ReviewDisplayDto> cachedList = List.of(reviewDisplayDto1, reviewDisplayDto2);
        when(lruCache.get("allReviews")).thenReturn(cachedList);

        List<ReviewDisplayDto> result = reviewService.getAllReviews();

        assertEquals(cachedList, result);
        verify(reviewRepository, never()).findAll();
        verify(lruCache).get("allReviews");
        verify(lruCache, never()).put(anyString(), any());
    }

    @Test
    void getAllReviews_CacheMiss() {
        when(lruCache.get("allReviews")).thenReturn(null);
        when(reviewRepository.findAll()).thenReturn(List.of(review1, review2));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);
        when(reviewMapper.toDto(review2)).thenReturn(reviewDisplayDto2);

        List<ReviewDisplayDto> result = reviewService.getAllReviews();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(lruCache).get("allReviews");
        verify(reviewRepository).findAll();
        verify(reviewMapper, times(2)).toDto(any(Review.class));
        verify(lruCache).put(eq("allReviews"), eq(List.of(reviewDisplayDto1, reviewDisplayDto2)));
    }

    @Test
    void getReviewById_CacheHit() {
        String cacheKey = buildKey("reviewById", review1.getId());
        when(lruCache.get(cacheKey)).thenReturn(reviewDisplayDto1);

        ReviewDisplayDto result = reviewService.getReviewById(review1.getId());

        assertEquals(reviewDisplayDto1, result);
        verify(reviewRepository, never()).findById(anyInt());
        verify(lruCache).get(cacheKey);
        verify(lruCache, never()).put(anyString(), any());
    }

    @Test
    void getReviewById_CacheMiss_Found() {
        String cacheKey = buildKey("reviewById", review1.getId());
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(review1));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);

        ReviewDisplayDto result = reviewService.getReviewById(review1.getId());

        assertEquals(reviewDisplayDto1, result);
        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findById(review1.getId());
        verify(reviewMapper).toDto(review1);
        verify(lruCache).put(cacheKey, reviewDisplayDto1);
    }

    @Test
    void getReviewById_CacheMiss_NotFound() {
        String cacheKey = buildKey("reviewById", 9999);
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findById(9999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewById(9999));

        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findById(9999);
        verify(reviewMapper, never()).toDto(any());
        verify(lruCache, never()).put(anyString(), any());
    }


    @Test
    void deleteReview_Success() {
        String cacheKey = buildKey("reviewById", review1.getId());
        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(review1));
        doNothing().when(reviewRepository).deleteById(review1.getId());

        reviewService.deleteReview(review1.getId());

        verify(reviewRepository).findById(review1.getId());
        verify(reviewRepository).deleteById(review1.getId());

        verify(lruCache).remove("allReviews");
        verify(lruCache).remove("reviewCountsPerTeacherNative");
        verify(lruCache).remove(buildKey("reviewsByTeacherId", teacher.getId()));
        verify(lruCache).remove(buildKey("reviewsByUserId", user.getId()));
        verify(lruCache).remove(buildKey("reviewsByUsername", user.getUsername()));
        verify(lruCache).remove(cacheKey);
    }

    @Test
    void deleteReview_NotFound() {
        when(reviewRepository.findById(9999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.deleteReview(9999));

        verify(reviewRepository).findById(9999);
        verify(reviewRepository, never()).deleteById(anyInt());
        verify(lruCache, never()).remove(anyString());
    }

    @Test
    void updateReview_Success_AllFieldsChanged() {
        String cacheKey = buildKey("reviewById", review1.getId());
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(4); // Change
        updateDto.setComment("Good"); // Change
        updateDto.setDate(testDate.plusDays(1)); // Change

        Review foundReview = new Review(review1.getId(), user, teacher, subject, review1.getDate(), review1.getGrade(), review1.getComment());
        Review updatedReview = new Review(review1.getId(), user, teacher, subject, testDate.plusDays(1), 4, "Good");
        ReviewDisplayDto updatedDisplayDto = new ReviewDisplayDto(updatedReview.getId(), user.getUsername(), teacherDisplayDto, subject.getName(), updatedReview.getDate(), updatedReview.getGrade(), updatedReview.getComment());

        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toDto(updatedReview)).thenReturn(updatedDisplayDto);

        ReviewDisplayDto result = reviewService.updateReview(review1.getId(), updateDto);

        assertNotNull(result);
        assertEquals(4, result.getGrade());
        assertEquals("Good", result.getComment());
        assertEquals(testDate.plusDays(1), result.getDate());

        verify(reviewRepository).findById(review1.getId());
        verify(reviewRepository).save(reviewCaptor.capture());
        assertEquals(4, reviewCaptor.getValue().getGrade());
        assertEquals("Good", reviewCaptor.getValue().getComment());
        assertEquals(testDate.plusDays(1), reviewCaptor.getValue().getDate());
        verify(reviewMapper).toDto(updatedReview);

        verify(lruCache, atLeastOnce()).remove(anyString());
        verify(lruCache).put(cacheKey, updatedDisplayDto);
    }

    @Test
    void updateReview_Success_OnlyGradeChanged() {
        String cacheKey = buildKey("reviewById", review1.getId());
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(4); // Change
        updateDto.setComment(review1.getComment()); // Same
        updateDto.setDate(review1.getDate()); // Same

        Review foundReview = new Review(review1.getId(), user, teacher, subject, review1.getDate(), review1.getGrade(), review1.getComment());
        Review updatedReview = new Review(review1.getId(), user, teacher, subject, review1.getDate(), 4, review1.getComment());
        ReviewDisplayDto updatedDisplayDto = new ReviewDisplayDto(updatedReview.getId(), user.getUsername(), teacherDisplayDto, subject.getName(), updatedReview.getDate(), updatedReview.getGrade(), updatedReview.getComment());


        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toDto(updatedReview)).thenReturn(updatedDisplayDto);

        ReviewDisplayDto result = reviewService.updateReview(review1.getId(), updateDto);

        assertEquals(4, result.getGrade());
        assertEquals(review1.getComment(), result.getComment());
        assertEquals(review1.getDate(), result.getDate());
        verify(reviewRepository).save(any(Review.class));
        verify(lruCache).put(cacheKey, updatedDisplayDto);
    }

    @Test
    void updateReview_Success_OnlyCommentChanged() {
        String cacheKey = buildKey("reviewById", review1.getId());
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(review1.getGrade()); // Same
        updateDto.setComment("Updated Comment"); // Change
        updateDto.setDate(review1.getDate()); // Same

        Review foundReview = new Review(review1.getId(), user, teacher, subject, review1.getDate(), review1.getGrade(), review1.getComment());
        Review updatedReview = new Review(review1.getId(), user, teacher, subject, review1.getDate(), review1.getGrade(), "Updated Comment");
        ReviewDisplayDto updatedDisplayDto = new ReviewDisplayDto(updatedReview.getId(), user.getUsername(), teacherDisplayDto, subject.getName(), updatedReview.getDate(), updatedReview.getGrade(), updatedReview.getComment());


        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toDto(updatedReview)).thenReturn(updatedDisplayDto);

        ReviewDisplayDto result = reviewService.updateReview(review1.getId(), updateDto);

        assertEquals(review1.getGrade(), result.getGrade());
        assertEquals("Updated Comment", result.getComment());
        assertEquals(review1.getDate(), result.getDate());
        verify(reviewRepository).save(any(Review.class));
        verify(lruCache).put(cacheKey, updatedDisplayDto);
    }

    @Test
    void updateReview_Success_OnlyDateChanged() {
        String cacheKey = buildKey("reviewById", review1.getId());
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(review1.getGrade()); // Same
        updateDto.setComment(review1.getComment()); // Same
        updateDto.setDate(testDate.minusDays(5)); // Change

        Review foundReview = new Review(review1.getId(), user, teacher, subject, review1.getDate(), review1.getGrade(), review1.getComment());
        Review updatedReview = new Review(review1.getId(), user, teacher, subject, testDate.minusDays(5), review1.getGrade(), review1.getComment());
        ReviewDisplayDto updatedDisplayDto = new ReviewDisplayDto(updatedReview.getId(), user.getUsername(), teacherDisplayDto, subject.getName(), updatedReview.getDate(), updatedReview.getGrade(), updatedReview.getComment());


        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(foundReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toDto(updatedReview)).thenReturn(updatedDisplayDto);

        ReviewDisplayDto result = reviewService.updateReview(review1.getId(), updateDto);

        assertEquals(review1.getGrade(), result.getGrade());
        assertEquals(review1.getComment(), result.getComment());
        assertEquals(testDate.minusDays(5), result.getDate());
        verify(reviewRepository).save(any(Review.class));
        verify(lruCache).put(cacheKey, updatedDisplayDto);
    }

    @Test
    void updateReview_Success_DtoFieldsNull() {
        String cacheKey = buildKey("reviewById", review1.getId());
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(null); // Null
        updateDto.setComment(null); // Null
        updateDto.setDate(null); // Null

        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(review1));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1); // Expect original DTO

        ReviewDisplayDto result = reviewService.updateReview(review1.getId(), updateDto);

        assertEquals(reviewDisplayDto1, result); // No changes expected

        verify(reviewRepository).findById(review1.getId());
        verify(reviewRepository, never()).save(any(Review.class)); // Save not called
        verify(reviewMapper).toDto(review1);
        verify(lruCache, never()).remove(anyString());
        verify(lruCache, never()).put(anyString(), any());
    }


    @Test
    void updateReview_Success_NoChanges() {
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(review1.getGrade());
        updateDto.setComment(review1.getComment());
        updateDto.setDate(review1.getDate());


        when(reviewRepository.findById(review1.getId())).thenReturn(Optional.of(review1));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);


        ReviewDisplayDto result = reviewService.updateReview(review1.getId(), updateDto);

        assertEquals(reviewDisplayDto1, result);

        verify(reviewRepository).findById(review1.getId());
        verify(reviewRepository, never()).save(any(Review.class));
        verify(reviewMapper).toDto(review1);
        verify(lruCache, never()).remove(anyString());
        verify(lruCache, never()).put(anyString(), any());
    }

    @Test
    void updateReview_NotFound() {
        ReviewCreateDto updateDto = new ReviewCreateDto();
        updateDto.setGrade(4);
        when(reviewRepository.findById(9999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.updateReview(9999, updateDto));

        verify(reviewRepository).findById(9999);
        verify(reviewRepository, never()).save(any());
        verify(lruCache, never()).remove(anyString());
        verify(lruCache, never()).put(anyString(), any());
    }

    @Test
    void getReviewsByTeacherId_CacheHit_Success() {
        String cacheKey = buildKey("reviewsByTeacherId", teacher.getId());
        List<ReviewDisplayDto> cachedList = List.of(reviewDisplayDto1);
        when(lruCache.get(cacheKey)).thenReturn(cachedList);

        List<ReviewDisplayDto> result = reviewService.getReviewsByTeacherId(teacher.getId());

        assertEquals(cachedList, result);
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).findByTeacherId(anyInt());
    }

    @Test
    void getReviewsByTeacherId_CacheHit_EmptyLeadsToNotFound() {
        String cacheKey = buildKey("reviewsByTeacherId", teacher.getId());
        when(lruCache.get(cacheKey)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByTeacherId(teacher.getId()));
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).findByTeacherId(anyInt());
    }


    @Test
    void getReviewsByTeacherId_CacheMiss_Found() {
        String cacheKey = buildKey("reviewsByTeacherId", teacher.getId());
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findByTeacherId(teacher.getId())).thenReturn(List.of(review1));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);

        List<ReviewDisplayDto> result = reviewService.getReviewsByTeacherId(teacher.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reviewDisplayDto1, result.get(0));
        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findByTeacherId(teacher.getId());
        verify(reviewMapper).toDto(review1);
        verify(lruCache).put(cacheKey, List.of(reviewDisplayDto1));
    }

    @Test
    void getReviewsByTeacherId_CacheMiss_NotFound() {
        String cacheKey = buildKey("reviewsByTeacherId", 99);
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findByTeacherId(99)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByTeacherId(99));

        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findByTeacherId(99);
        verify(reviewMapper, never()).toDto(any());
        verify(lruCache, never()).put(anyString(), any());
    }


    @Test
    void getReviewsByUserId_CacheHit_Success() {
        String cacheKey = buildKey("reviewsByUserId", user.getId());
        List<ReviewDisplayDto> cachedList = List.of(reviewDisplayDto1);
        when(lruCache.get(cacheKey)).thenReturn(cachedList);

        List<ReviewDisplayDto> result = reviewService.getReviewsByUserId(user.getId());

        assertEquals(cachedList, result);
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).findByUserId(anyInt());
    }

    @Test
    void getReviewsByUserId_CacheHit_EmptyLeadsToNotFound() {
        String cacheKey = buildKey("reviewsByUserId", user.getId());
        when(lruCache.get(cacheKey)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByUserId(user.getId()));
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).findByUserId(anyInt());
    }


    @Test
    void getReviewsByUserId_CacheMiss_Found() {
        String cacheKey = buildKey("reviewsByUserId", user.getId());
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findByUserId(user.getId())).thenReturn(List.of(review1));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);

        List<ReviewDisplayDto> result = reviewService.getReviewsByUserId(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reviewDisplayDto1, result.get(0));
        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findByUserId(user.getId());
        verify(reviewMapper).toDto(review1);
        verify(lruCache).put(cacheKey, List.of(reviewDisplayDto1));
    }

    @Test
    void getReviewsByUserId_CacheMiss_NotFound() {
        String cacheKey = buildKey("reviewsByUserId", 99);
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findByUserId(99)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByUserId(99));

        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findByUserId(99);
        verify(reviewMapper, never()).toDto(any());
        verify(lruCache, never()).put(anyString(), any());
    }


    @Test
    void getReviewsByUserUsername_CacheHit_Success() {
        String cacheKey = buildKey("reviewsByUsername", user.getUsername());
        List<ReviewDisplayDto> cachedList = List.of(reviewDisplayDto1);
        when(lruCache.get(cacheKey)).thenReturn(cachedList);

        List<ReviewDisplayDto> result = reviewService.getReviewsByUserUsername(user.getUsername());

        assertEquals(cachedList, result);
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).findByUserUsernameIgnoreCase(anyString());
    }

    @Test
    void getReviewsByUserUsername_CacheHit_EmptyLeadsToNotFound() {
        String cacheKey = buildKey("reviewsByUsername", user.getUsername());
        when(lruCache.get(cacheKey)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByUserUsername(user.getUsername()));
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).findByUserUsernameIgnoreCase(anyString());
    }


    @Test
    void getReviewsByUserUsername_CacheMiss_Found() {
        String cacheKey = buildKey("reviewsByUsername", user.getUsername());
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findByUserUsernameIgnoreCase(user.getUsername())).thenReturn(List.of(review1));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);

        List<ReviewDisplayDto> result = reviewService.getReviewsByUserUsername(user.getUsername());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(reviewDisplayDto1, result.get(0));
        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findByUserUsernameIgnoreCase(user.getUsername());
        verify(reviewMapper).toDto(review1);
        verify(lruCache).put(cacheKey, List.of(reviewDisplayDto1));
    }

    @Test
    void getReviewsByUserUsername_CacheMiss_NotFound() {
        String cacheKey = buildKey("reviewsByUsername", "nonexistent");
        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.findByUserUsernameIgnoreCase("nonexistent")).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsByUserUsername("nonexistent"));

        verify(lruCache).get(cacheKey);
        verify(reviewRepository).findByUserUsernameIgnoreCase("nonexistent");
        verify(reviewMapper, never()).toDto(any());
        verify(lruCache, never()).put(anyString(), any());
    }

    @Test
    void getReviewCountsPerTeacher_CacheHit() {
        String cacheKey = "reviewCountsPerTeacherNative";
        List<Object[]> cachedCounts = new ArrayList<>();
        cachedCounts.add(new Object[]{ "Ivanov", 10L });
        when(lruCache.get(cacheKey)).thenReturn(cachedCounts);

        List<Object[]> result = reviewService.getReviewCountsPerTeacher();

        assertEquals(cachedCounts, result);
        verify(lruCache).get(cacheKey);
        verify(reviewRepository, never()).countReviewsPerTeacher();
    }

    @Test
    void getReviewCountsPerTeacher_CacheMiss() {
        String cacheKey = "reviewCountsPerTeacherNative";
        List<Object[]> dbCounts = new ArrayList<>();
        dbCounts.add(new Object[]{ "Ivanov", 10L });

        when(lruCache.get(cacheKey)).thenReturn(null);
        when(reviewRepository.countReviewsPerTeacher()).thenReturn(dbCounts);

        List<Object[]> result = reviewService.getReviewCountsPerTeacher();

        assertEquals(dbCounts, result);
        verify(lruCache).get(cacheKey);
        verify(reviewRepository).countReviewsPerTeacher();
        verify(lruCache).put(cacheKey, dbCounts);
    }

    @Test
    void searchReviews_Success() {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();
        when(reviewRepository.searchReviews(start, end, "Ivanov", "Math", 4)).thenReturn(List.of(review1, review2));
        when(reviewMapper.toDto(review1)).thenReturn(reviewDisplayDto1);
        when(reviewMapper.toDto(review2)).thenReturn(reviewDisplayDto2);

        List<ReviewDisplayDto> result = reviewService.searchReviews(start, end, "Ivanov", "Math", 4);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(reviewRepository).searchReviews(start, end, "Ivanov", "Math", 4);
        verify(reviewMapper, times(2)).toDto(any(Review.class));
        verifyNoInteractions(lruCache);
    }

    @Test
    void searchReviews_NotFound() {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();
        when(reviewRepository.searchReviews(start, end, "Petrov", "Physics", 5)).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.searchReviews(start, end, "Petrov", "Physics", 5));

        verify(reviewRepository).searchReviews(start, end, "Petrov", "Physics", 5);
        verify(reviewMapper, never()).toDto(any());
        verifyNoInteractions(lruCache);
    }
}