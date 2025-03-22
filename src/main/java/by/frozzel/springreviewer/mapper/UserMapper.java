package by.frozzel.springreviewer.mapper;

import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toEntity(UserCreateDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        return user;
    }

    public UserDisplayDto toDto(User user) {
        return new UserDisplayDto(user.getId(), user.getUsername());
    }
}
