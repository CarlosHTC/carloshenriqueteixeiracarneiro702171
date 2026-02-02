package br.com.avaliacao.apimusicmanagement.api.v1.auth;

import br.com.avaliacao.apimusicmanagement.api.v1.auth.dto.AuthResponse;
import br.com.avaliacao.apimusicmanagement.api.v1.auth.dto.LoginRequest;
import br.com.avaliacao.apimusicmanagement.api.v1.auth.dto.RefreshRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        var tokens = authService.login(loginRequest.username(), loginRequest.password());
        return ResponseEntity.ok(new AuthResponse(tokens.accesToken(), tokens.refreshToken(), authService.accessExpMs(), authService.refreshExpMs()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest refreshRequest) {
        var tokens = authService.refresh(refreshRequest.refreshToken());
        return ResponseEntity.ok(new AuthResponse(tokens.accesToken(), tokens.refreshToken(), authService.accessExpMs(), authService.refreshExpMs()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest refreshRequest) {
        authService.logout(refreshRequest.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
