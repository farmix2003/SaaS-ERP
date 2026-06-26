package farmix.com.backend.auth.service;

import farmix.com.backend.auth.dto.AuthResponse;
import farmix.com.backend.auth.dto.LoginRequest;
import farmix.com.backend.auth.dto.RegisterCompanyRequest;
import farmix.com.backend.auth.dto.RegisterCompanyResponse;
import farmix.com.backend.common.exception.BadRequestException;
import farmix.com.backend.company.entity.Company;
import farmix.com.backend.security.JwtService;
import farmix.com.backend.security.UserPrincipal;
import farmix.com.backend.user.entity.User;
import farmix.com.backend.user.entity.UserRole;
import farmix.com.backend.user.entity.UserStatus;
import farmix.com.backend.repository.CompanyRepository;
import farmix.com.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager  authenticationManager;

    @Transactional
    public RegisterCompanyResponse createCompany(RegisterCompanyRequest company) {

       if (userRepository.existsByEmail(company.email())){
           throw new BadRequestException("Email already exists");
       }

       Company newCompany = Company.builder()
               .name(company.companyName())
               .industry(company.industry())
               .phoneNumber(company.phone())
               .email(company.companyEmail())
               .build();

       Company savedCompany = companyRepository.save(newCompany);

       User adminUser = User.builder()
                .company(savedCompany)
                .firstName(company.firstName())
                .lastName(company.lastName())
                .email(company.email())
                .password(passwordEncoder.encode(company.password()))
                .role(UserRole.COMPANY_ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(adminUser);

        return new RegisterCompanyResponse(
                savedCompany.getId(),
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Company registered successfully"
                );
    }

    public AuthResponse login(LoginRequest loginRequest){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();

        assert userPrincipal != null;
        String accessToken = jwtService.generateAccessToken(userPrincipal);

        return new AuthResponse(
                accessToken,
                "Bearer",
                userPrincipal.getId(),
                userPrincipal.getCompanyId(),
                userPrincipal.getEmail(),
                userPrincipal.getRole()
        );
    }

}
