package farmix.com.backend.user.mapper;

import farmix.com.backend.user.dto.UserResponse;
import farmix.com.backend.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getCompany().getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
