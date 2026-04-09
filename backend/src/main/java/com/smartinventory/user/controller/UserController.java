package com.smartinventory.user.controller;

import com.smartinventory.user.dto.UserResponse;
import com.smartinventory.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Kullanıcı Yönetimi", description = "Kullanıcı CRUD işlemleri")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Tüm kullanıcıları listele",
        description = "Sadece Depo Sorumlusu erişebilir",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim")
        }
    )
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or #id == authentication.principal.id")
    @Operation(
        summary = "ID ile kullanıcı getir",
        description = "Depo Sorumlusu herkesi, çalışan sadece kendisini görebilir",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
        }
    )
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Kullanıcıyı deaktif et",
        description = "Sadece Depo Sorumlusu kullanıcıları deaktif edebilir",
        responses = {
            @ApiResponse(responseCode = "204", description = "Başarıyla deaktif edildi"),
            @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
        }
    )
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
