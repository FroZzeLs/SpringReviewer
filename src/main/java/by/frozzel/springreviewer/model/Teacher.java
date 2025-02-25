package by.frozzel.springreviewer.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Teacher {
    private int id;
    private String name;
    private String subject;
}