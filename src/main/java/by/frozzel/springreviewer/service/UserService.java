package by.frozzel.springreviewer.service;

import by.frozzel.springreviewer.config.LruCache;
import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.mapper.UserMapper;
import by.frozzel.springreviewer.model.User;
import by.frozzel.springreviewer.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private final LruCache<String, Object> lruCache;

    private String generateCacheKey(String prefix, Object... params) {
        StringBuilder keyBuilder = new StringBuilder(prefix);
        for (Object param : params) {
            keyBuilder.append(":");
            keyBuilder.append(param == null ? "null" : param.toString());
        }
        return keyBuilder.toString();
    }

    @Transactional
    public UserDisplayDto createUser(UserCreateDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Username already exists: " + dto.getUsername());
        }
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        lruCache.clear();
        return userMapper.toDto(savedUser);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<UserDisplayDto> getAllUsers() {
        String cacheKey = generateCacheKey("allUsers");
        List<UserDisplayDto> cachedUsers = (List<UserDisplayDto>) lruCache.get(cacheKey);
        if (cachedUsers != null) {
            return cachedUsers;
        } else {
            List<UserDisplayDto> users = userRepository.findAll().stream()
                    .map(userMapper::toDto)
                    .toList();
            lruCache.put(cacheKey, users);
            return users;
        }
    }

    @Transactional(readOnly = true)
    public Optional<UserDisplayDto> getUserById(Integer id) {
        String cacheKey = generateCacheKey("userById", id);
        UserDisplayDto cachedUser = (UserDisplayDto) lruCache.get(cacheKey);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        } else {
            Optional<UserDisplayDto> userOpt = userRepository.findById(id).map(userMapper::toDto);
            userOpt.ifPresent(dto -> lruCache.put(cacheKey, dto));
            return userOpt;
        }
    }

    @Transactional(readOnly = true)
    public Optional<UserDisplayDto> getUserByUsername(String username) {
        String cacheKey = generateCacheKey("userByUsername", username);
        UserDisplayDto cachedUser = (UserDisplayDto) lruCache.get(cacheKey);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        } else {
            Optional<UserDisplayDto> userOpt = userRepository
                    .findByUsername(username).map(userMapper::toDto);
            userOpt.ifPresent(dto -> lruCache.put(cacheKey, dto));
            return userOpt;
        }
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
                    lruCache.clear();
                    return Optional.of(userMapper.toDto(updatedUser));
                })
                .orElse(Optional.empty());
    }

    @Transactional
    public boolean deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            lruCache.clear();
            return true;
        }
        return false;
    }
}