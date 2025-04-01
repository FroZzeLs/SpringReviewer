package by.frozzel.springreviewer.repository;

import by.frozzel.springreviewer.model.Review;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByTeacherId(Integer teacherId);

    List<Review> findByUserId(Integer userId);

    List<Review> findByUserUsername(String username);

    @Transactional
    @Modifying
    @Query("DELETE FROM Review r WHERE r.subject.id = :subjectId")
    void deleteBySubjectId(@Param("subjectId") int subjectId);

    @Query(value = "SELECT t.surname, COUNT(r.id) as review_count " +
            "FROM reviews r JOIN teachers t ON r.teacher_id = t.id " +
            "GROUP BY t.id, t.surname " +
            "ORDER BY review_count DESC",
            nativeQuery = true)
    List<Object[]> countReviewsPerTeacherNative();
}