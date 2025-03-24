package by.frozzel.springreviewer.mapper;

import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.model.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private final ReviewMapper reviewMapper;

    public User toEntity(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        return user;
    }

    public UserDisplayDto toDto(User user) {
        return new UserDisplayDto(
                user.getId(),
                user.getUsername(),
                user.getReviews() != null
                        ? user.getReviews().stream().map(
                                reviewMapper::toDto).collect(Collectors.toList())
                        : List.of()
        );
    }
}
