package by.frozzel.springreviewer.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class ReviewCreateDto {
    private Integer userId;
    private Integer teacherId;
    private Integer subjectId;
    private LocalDate date;
    private Integer grade;
    private String comment;
}
