package by.frozzel.springreviewer.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDisplayDto {
    private Integer id;
    private String name;
    private List<String> teacherNames;
}
