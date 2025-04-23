package by.frozzel.springreviewer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.exception.ConflictException;
import by.frozzel.springreviewer.exception.ResourceNotFoundException;
import by.frozzel.springreviewer.mapper.UserMapper;
import by.frozzel.springreviewer.model.User;
import by.frozzel.springreviewer.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private UserCreateDto userCreateDto;
    private UserDisplayDto userDisplayDto1;
    private UserDisplayDto userDisplayDto2;

    @BeforeEach
    void setUp() {
        user1 = new User(1, "testuser1", Collections.emptyList());
        user2 = new User(2, "testuser2", Collections.emptyList());
        userCreateDto = new UserCreateDto("testuser1");
        userDisplayDto1 = new UserDisplayDto(1, "testuser1", Collections.emptyList());
        userDisplayDto2 = new UserDisplayDto(2, "testuser2", Collections.emptyList());
    }

    @Test
    void createUser_Success() {
        when(userRepository.findByUsernameIgnoreCase("testuser1")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userCreateDto)).thenReturn(user1);
        when(userRepository.save(user1)).thenReturn(user1);
        when(userMapper.toDto(user1)).thenReturn(userDisplayDto1);

        UserDisplayDto result = userService.createUser(userCreateDto);

        assertNotNull(result);
        assertEquals("testuser1", result.getUsername());
        verify(userRepository).findByUsernameIgnoreCase("testuser1");
        verify(userMapper).toEntity(userCreateDto);
        verify(userRepository).save(user1);
        verify(userMapper).toDto(user1);
    }

    @Test
    void createUser_Conflict() {
        when(userRepository.findByUsernameIgnoreCase("testuser1")).thenReturn(Optional.of(user1));

        assertThrows(ConflictException.class, () -> userService.createUser(userCreateDto));

        verify(userRepository).findByUsernameIgnoreCase("testuser1");
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDisplayDto1);
        when(userMapper.toDto(user2)).thenReturn(userDisplayDto2);

        List<UserDisplayDto> results = userService.getAllUsers();

        assertNotNull(results);
        assertEquals(2, results.size());
        verify(userRepository).findAll();
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userMapper.toDto(user1)).thenReturn(userDisplayDto1);

        UserDisplayDto result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(userRepository).findById(1);
        verify(userMapper).toDto(user1);
    }

    @Test
    void getUserById_NotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99));

        verify(userRepository).findById(99);
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void getUserByUsername_Success() {
        when(userRepository.findByUsernameIgnoreCase("testuser1")).thenReturn(Optional.of(user1));
        when(userMapper.toDto(user1)).thenReturn(userDisplayDto1);

        UserDisplayDto result = userService.getUserByUsername("testuser1");

        assertNotNull(result);
        assertEquals("testuser1", result.getUsername());
        verify(userRepository).findByUsernameIgnoreCase("testuser1");
        verify(userMapper).toDto(user1);
    }

    @Test
    void getUserByUsername_NotFound() {
        when(userRepository.findByUsernameIgnoreCase("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("nonexistent"));

        verify(userRepository).findByUsernameIgnoreCase("nonexistent");
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_Success_NewUsername() {
        UserCreateDto updateDto = new UserCreateDto("newuser");
        User updatedUser = new User(1, "newuser", Collections.emptyList());
        UserDisplayDto updatedDisplayDto = new UserDisplayDto(1, "newuser", Collections.emptyList());

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findByUsernameIgnoreCase("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(userMapper.toDto(any(User.class))).thenReturn(updatedDisplayDto);

        UserDisplayDto result = userService.updateUser(1, updateDto);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).findById(1);
        verify(userRepository).findByUsernameIgnoreCase("newuser");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(any(User.class));
    }

    @Test
    void updateUser_Success_SameUsername() {
        UserCreateDto updateDto = new UserCreateDto("testuser1");

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.save(any(User.class))).thenReturn(user1);
        when(userMapper.toDto(user1)).thenReturn(userDisplayDto1);

        UserDisplayDto result = userService.updateUser(1, updateDto);

        assertNotNull(result);
        assertEquals("testuser1", result.getUsername());
        verify(userRepository).findById(1);
        verify(userRepository, never()).findByUsernameIgnoreCase(anyString());
        verify(userRepository).save(user1);
        verify(userMapper).toDto(user1);
    }


    @Test
    void updateUser_NotFound() {
        UserCreateDto updateDto = new UserCreateDto("newuser");

        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99, updateDto));

        verify(userRepository).findById(99);
        verify(userRepository, never()).findByUsernameIgnoreCase(anyString());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_Conflict() {
        UserCreateDto updateDto = new UserCreateDto("testuser2");

        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findByUsernameIgnoreCase("testuser2")).thenReturn(Optional.of(user2));

        assertThrows(ConflictException.class, () -> userService.updateUser(1, updateDto));

        verify(userRepository).findById(1);
        verify(userRepository).findByUsernameIgnoreCase("testuser2");
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }


    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        doNothing().when(userRepository).deleteById(1);

        userService.deleteUser(1);

        verify(userRepository).findById(1);
        verify(userRepository).deleteById(1);
    }

    @Test
    void deleteUser_NotFound() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99));

        verify(userRepository).findById(99);
        verify(userRepository, never()).deleteById(anyInt());
    }
}