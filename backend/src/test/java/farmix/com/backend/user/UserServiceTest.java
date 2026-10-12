package farmix.com.backend.user;

import farmix.com.backend.common.exception.BadRequestException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.company.repository.CompanyRepository;
import farmix.com.backend.security.CurrentUser;
import farmix.com.backend.user.dto.ChangeUserRoleRequest;
import farmix.com.backend.user.dto.CreateUserRequest;
import farmix.com.backend.user.dto.UserResponse;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.entity.UserRole;
import farmix.com.backend.user.entity.UserStatus;
import farmix.com.backend.user.mapper.UserMapper;
import farmix.com.backend.user.repository.UserRepository;
import farmix.com.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private UserService service;

    private Company testCompany;
    private User testUser;

    @BeforeEach
    void setUp(){
        testCompany = Company.builder()
                .id(100L)
                .name("FarDev")
                .build();

        testUser = User.builder()
                .id(1L)
                .company(testCompany)
                .firstName("John")
                .lastName("Doe")
                .email("jon@fardev.com")
                .role(UserRole.MANAGER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests{

        @Test
        @DisplayName("Should create a new user successfully")
        void create_ShouldCreateUser_WhenValidRequest(){
            CreateUserRequest createUserRequest = new CreateUserRequest(
                    "Jane",
                    "Doe",
                    "jane@fardev.com",
                    "secret1234",
                    UserRole.MANAGER
            );
          when(currentUser.getCompanyId()).thenReturn(100L);
          when(userRepository.existsByEmail("jane@fardev.com")).thenReturn(false);
          when(companyRepository.getReferenceById(100L)).thenReturn(testCompany);
          when(passwordEncoder.encode("secret1234")).thenReturn("encode_pass");
          when(userRepository.save(any(User.class))).thenReturn(testUser);
          when(userMapper.toResponse(any(User.class))).thenReturn(
                  new UserResponse(
                          1L, 100L,
                          "Jane", "Smith",
                          "jane@acme.com",
                          UserRole.MANAGER, UserStatus.ACTIVE,
                          null, null
                  )
          );

          UserResponse response = service.create(createUserRequest);

          assertThat(response).isNotNull();
          assertThat(response.email()).isEqualTo("jane@acme.com");
          verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when attempting to create a user with SUPER_ADMIN role")
        void create_ShouldThrowException_WhenRoleIsSuperAdmin(){

           // Arrange
            CreateUserRequest createUserRequest = new CreateUserRequest(
                    "Jane",
                    "Doe",
                    "jane@fardev.com",
                    "secret1234",
                    UserRole.SUPER_ADMIN
            );
            when(currentUser.getCompanyId()).thenReturn(100L);

            //this test ensures that the service throws an IllegalArgumentException when trying to create a user with the SUPER_ADMIN role
            assertThatThrownBy(() -> service.create(createUserRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot create a user with role SUPER_ADMIN");

            //this test ensures that the userRepository.save method is never called when the role is SUPER_ADMIN
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when attempting to create a user with an existing email")
        void create_ShouldThrowException_WhenEmailAlreadyExists(){
            // Arrange
            CreateUserRequest createUserRequest = new CreateUserRequest(
                    "Jane",
                    "Doe",
                    "jane@fardev.com",
                    "secret1234",
                    UserRole.MANAGER
            );
            // Mock the currentUser to return a specific companyId
            when(currentUser.getCompanyId()).thenReturn(100L);
            when(userRepository.existsByEmail("jane@fardev.com")).thenReturn(true);

            //this test ensures that the service throws an IllegalArgumentException when trying to create a user with an existing email
            assertThatThrownBy(() -> service.create(createUserRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User with email jane@fardev.com already exists");

            //this test ensures that the userRepository.save method is never called when the email already exists
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Self-Action & Security Guards Tests")
    class  SelfActionTestes{

        @Test
        @DisplayName("Should prevent deactivating yourself")
        void deactivate_ShouldThrowException_WhenUserTriesToDeactivateSelf(){
            when(currentUser.getCompanyId()).thenReturn(100L);
            when(currentUser.getUserId()).thenReturn(1L);

            assertThatThrownBy(() -> service.deactivate(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("You can not deactivate yourself!");
            verify(userRepository, never()).save(any());
        }
        @Test
        @DisplayName("Should prevent a user from changing their own role")
        void changeRole_ShouldThrowException_WhenUserTriesToChangeOwnRole(){

            ChangeUserRoleRequest req = new ChangeUserRoleRequest(UserRole.MANAGER);

            when(currentUser.getCompanyId()).thenReturn(100L);
            when(currentUser.getUserId()).thenReturn(1L);

            assertThatThrownBy(() -> service.changeRole(1L, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("You can not change your own role");
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should deactivate a user successfully when target is another user")
        void deactivate_ShouldDeactivateUser_WhenTargetIsAnotherUser(){
            when(currentUser.getCompanyId()).thenReturn(100L);
            when(currentUser.getUserId()).thenReturn(99L);
            when(userRepository.findByIdAndCompany_Id(1L, 100L)).thenReturn(java.util.Optional.of(testUser));
            when(userRepository.save(testUser)).thenReturn(testUser);

            service.deactivate(1L);

            assertThat(testUser.getStatus()).isEqualTo(UserStatus.INACTIVE);
            verify(userRepository).save(testUser);
        }
    }

}
