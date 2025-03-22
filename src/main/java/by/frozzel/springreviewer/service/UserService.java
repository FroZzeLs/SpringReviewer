package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.mapper.UserMapper;
import by.frozzel.springreviewer.model.User;
import by.frozzel.springreviewer.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDisplayDto createUser(UserCreateDto dto) {
        User user = userMapper.toEntity(dto);
        return userMapper.toDto(userRepository.save(user));
    }

    public List<UserDisplayDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDisplayDto> getUserById(Integer id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    public Optional<UserDisplayDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDto);
    }

    @Transactional
    public Optional<UserDisplayDto> updateUser(Integer id, UserCreateDto dto) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(dto.getUsername());
                    return userMapper.toDto(userRepository.save(user));
                });
    }

    @Transactional
    public boolean deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
