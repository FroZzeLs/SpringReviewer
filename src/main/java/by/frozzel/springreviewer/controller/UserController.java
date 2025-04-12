package by.frozzel.springreviewer.controller;

import by.frozzel.springreviewer.dto.UserCreateDto;
import by.frozzel.springreviewer.dto.UserDisplayDto;
import by.frozzel.springreviewer.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDisplayDto createUser(@Valid @RequestBody UserCreateDto dto) {
        return userService.createUser(dto);
    }

    @GetMapping
    public List<UserDisplayDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserDisplayDto getUserById(@PathVariable @Min(value = 1,
            message = "User ID must be positive") Integer id) {
        return userService.getUserById(id);
    }

    @GetMapping("/username/{username}")
    public UserDisplayDto getUserByUsername(
            @PathVariable @NotBlank(message = "Username cannot be blank") String username) {
        return userService.getUserByUsername(username);
    }

    @PutMapping("/{id}")
    public UserDisplayDto updateUser(
            @PathVariable @Min(value = 1, message = "User ID must be positive") Integer id,
                                     @Valid @RequestBody UserCreateDto dto) {
        return userService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(
            @PathVariable @Min(value = 1, message = "User ID must be positive") Integer id) {
        userService.deleteUser(id);
    }
}