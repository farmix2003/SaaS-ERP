package farmix.com.backend.user.controller;

import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.CurrentUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final CurrentUser currentUser;

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.ok(new CurrentUserResponse(
                currentUser.getUserId(),
                currentUser.getCompanyId(),
                currentUser.getEmail(),
                currentUser.getRole()
        ));
    }

}
