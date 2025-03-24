package by.frozzel.springreviewer.dto;

import by.frozzel.springreviewer.model.Teacher;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewDisplayDto {
    private Integer id;
    private String author;
    private TeacherDisplayDto teacher;
    private String subjectName;
    private LocalDate date;
    private Integer grade;
    private String comment;
}

