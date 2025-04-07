package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.mapper.UserMapper;
import by.frozzel.springreviewer.model.User;
import by.frozzel.springreviewer.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDisplayDto createUser(UserCreateDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username already exists: " + dto.getUsername());
        }
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDisplayDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserDisplayDto> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<UserDisplayDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toDto);
    }

    @Transactional
    public Optional<UserDisplayDto> updateUser(Integer id, UserCreateDto dto) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (!existingUser.getUsername().equals(dto.getUsername())
                            && userRepository.findByUsername(dto.getUsername()).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT,
                                "Username already exists: " + dto.getUsername());
                    }
                    existingUser.setUsername(dto.getUsername());
                    User updatedUser = userRepository.save(existingUser);
                    return Optional.of(userMapper.toDto(updatedUser));
                })
                .orElse(Optional.empty());
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