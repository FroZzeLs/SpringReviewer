package by.frozzel.springreviewer.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ReviewCreateDto {
    private Integer userId;
    private Integer teacherId;
    private Integer subjectId;
    private LocalDate date;
    private Integer grade;
    private String comment;
}
