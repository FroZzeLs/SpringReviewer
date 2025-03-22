package by.frozzel.springreviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

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
