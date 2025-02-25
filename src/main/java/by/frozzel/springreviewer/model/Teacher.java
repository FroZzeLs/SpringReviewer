package by.frozzel.springreviewer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Teacher {
    private String surname;
    private String name;
    private String patronym;
    private String subject;
}