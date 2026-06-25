package farmix.com.backend.auth.controller;

import farmix.com.backend.auth.dto.RegisterCompanyRequest;
import farmix.com.backend.auth.dto.RegisterCompanyResponse;
import farmix.com.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register-company")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterCompanyResponse registerCompany(@Valid @RequestBody RegisterCompanyRequest request){
        return authService.createCompany(request);
    }

}
