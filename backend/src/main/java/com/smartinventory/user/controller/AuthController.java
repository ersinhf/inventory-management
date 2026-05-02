package com.smartinventory.user.controller;

import com.smartinventory.user.dto.AuthResponse;
import com.smartinventory.user.dto.LoginRequest;
import com.smartinventory.user.dto.RegisterRequest;
import com.smartinventory.user.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Kimlik Doğrulama", description = "Kayıt ve giriş işlemleri")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary = "Yeni kullanıcı kaydı (sadece Depo Sorumlusu)",
        description = "Yöneticinin sisteme yeni kullanıcı eklemesi için kullanılır",
        responses = {
            @ApiResponse(responseCode = "201", description = "Kullanıcı başarıyla oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek veya e-posta zaten kayıtlı"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim")
        }
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Kullanıcı girişi",
        description = "E-posta ve şifre ile giriş yapar, JWT token döner",
        responses = {
            @ApiResponse(responseCode = "200", description = "Giriş başarılı"),
            @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri")
        }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }
}
