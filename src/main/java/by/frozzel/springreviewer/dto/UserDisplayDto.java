package by.frozzel.springreviewer.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDisplayDto {
    private Integer id;
    private String username;
    private List<ReviewDisplayDto> reviews;
}
