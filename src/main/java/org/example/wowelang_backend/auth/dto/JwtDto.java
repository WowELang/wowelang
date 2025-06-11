package org.example.wowelang_backend.auth.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class JwtDto {
    private String accessToken;
    private String refreshToken;
}