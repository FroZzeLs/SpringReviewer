package by.frozzel.springreviewer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class TeacherDisplayDto {
    private Integer id;
    private String surname;
    private String name;
    private String patronym;

    @Builder.Default
    private List<String> subjects = new ArrayList<>(); // 🛠 Гарантируем, что список не будет `null`
}

