package by.frozzel.springreviewer.mapper;

import by.frozzel.springreviewer.dto.ReviewCreateDto;
import by.frozzel.springreviewer.dto.ReviewDisplayDto;
import by.frozzel.springreviewer.model.Review;
import by.frozzel.springreviewer.model.Subject;
import by.frozzel.springreviewer.model.Teacher;
import by.frozzel.springreviewer.model.User;
import by.frozzel.springreviewer.repository.SubjectRepository;
import by.frozzel.springreviewer.repository.TeacherRepository;
import by.frozzel.springreviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {
    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherMapper teacherMapper;

    public Review toEntity(ReviewCreateDto dto) {
        Review review = new Review();
        review.setDate(dto.getDate());
        review.setGrade(dto.getGrade());
        review.setComment(dto.getComment());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new RuntimeException("Teacher not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        review.setUser(user);
        review.setTeacher(teacher);
        review.setSubject(subject);

        return review;
    }

    public ReviewDisplayDto toDto(Review review) {
        return new ReviewDisplayDto(
                review.getId(),
                review.getUser().getUsername(),
                teacherMapper.toDto(review.getTeacher()),
                review.getSubject().getName(),
                review.getDate(),
                review.getGrade(),
                review.getComment()
        );
    }
}
