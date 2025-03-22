package by.frozzel.springreviewer.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewDisplayDto {
    private Integer id;
    private String username;
    private String teacherFullName;
    private String subjectName;
    private LocalDate date;
    private Integer grade;
    private String comment;
}
