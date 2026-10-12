package farmix.com.backend.user.controller;

import farmix.com.backend.common.dto.PageResponse;
import farmix.com.backend.user.dto.ChangeUserRoleRequest;
import farmix.com.backend.user.dto.CreateUserRequest;
import farmix.com.backend.user.dto.UpdateUserRequest;
import farmix.com.backend.user.dto.UserResponse;
import farmix.com.backend.user.entity.UserRole;
import farmix.com.backend.user.entity.UserStatus;
import farmix.com.backend.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<UserResponse> list(@RequestParam(required = false) String q,
                                           @RequestParam(required = false)UserRole role,
                                           @RequestParam(required = false)UserStatus status,
                                           Pageable pageable
                                           ) {
        return PageResponse.from(userService.list(q, role, status, pageable));
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @PutMapping("/{id}")
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return userService.update(id, request);
    }
    @PatchMapping("/{id}/role")
    public UserResponse changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserRoleRequest request
    ) {
        return userService.changeRole(id, request);
    }
    @PatchMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable Long id) {
        return userService.deactivate(id);
    }
    @PatchMapping("/{id}/activate")
    public UserResponse activate(@PathVariable Long id) {
        return userService.activate(id);
    }

}
