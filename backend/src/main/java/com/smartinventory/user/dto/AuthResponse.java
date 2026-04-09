package com.smartinventory.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kimlik doğrulama yanıtı")
public class AuthResponse {

    @Schema(description = "JWT erişim token'ı")
    private String accessToken;

    @Schema(description = "Token tipi", example = "Bearer")
    private String tokenType;

    @Schema(description = "Kullanıcı bilgileri")
    private UserResponse user;
}
