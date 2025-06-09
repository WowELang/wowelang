package org.example.wowelang_backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.auth.dto.LoginResponseDto;
import org.example.wowelang_backend.auth.dto.UserPrincipalDTO;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.security.custom.CustomUserDetailsService;
import org.example.wowelang_backend.user.domain.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 유효기간(초단위)
    @Value("${jwt.token-validity-in-seconds}")
    private Long tokenValidityInSeconds;

    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    // Bean 초기화 시점에, secretKey를 Base64로 인코딩하여 사용
    // JWT 라이브러리에서 요구하는 형식이며, 일관된 형식을 유지하기 위함
    @PostConstruct
    protected void init() {
        // secretKey가 null이 아니라면 인코딩 처리합니다.
        if (secretKey != null && !secretKey.isEmpty()) {
            secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        }
    }

    // 서명을 위한 Key 객체를 생성
    // Keys.hmacShaKeyFor()는 비밀키(byte[])를 받아 Key 객체를 생성합니다.
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

      /*JWT 토큰 생성
      @param userId 사용자 식별자 (예: user의 id 값)
      @param roles  사용자 권한 목록 (userType enum 사용)
      @return 생성된 JWT 토큰 문자열*/
    public String createAccessToken(Long userId, String loginId, List<String> roles){
        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInSeconds * 1000);

        // 토큰 빌더를 통해 토큰 생성 및 서명
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("login_id", loginId)
                .claim("roles", roles)   //subject 설정
                .setIssuedAt(now)           // 발행 시간
                .setExpiration(validity)    // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // HS256 알고리즘과 Key 객체를 사용해 서명
                .compact();
    }

    public String createRefreshToken(UserPrincipalDTO userPrincipalDTO, long refreshTtl) {

        String refreshToken = UUID.randomUUID().toString();

        User user = User.builder()
                .id(userPrincipalDTO.getId()).build();

        refreshTokenRepository.deleteByUser(user);

        refreshTokenRepository.save(RefreshToken.of(user, refreshToken, refreshTtl));

        return refreshToken;
    }

      /*JWT 토큰의 유효성 검증
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 그렇지 않으면 예외를 던짐*/
    public boolean validateAccessToken(String token) {
        try {
            // 토큰의 서명과 만료 시간을 검증합니다.
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;

        } catch (ExpiredJwtException ex) {
            throw new ResponseStatusException(
                    ErrorStatus.TOKEN_EXPIRED.getHttpStatus(),
                    ErrorStatus.TOKEN_EXPIRED.getMessage()
            );

        } catch (MalformedJwtException ex) {
            throw new ResponseStatusException(
                    ErrorStatus.TOKEN_MALFORMED.getHttpStatus(),
                    ErrorStatus.TOKEN_MALFORMED.getMessage()
            );

        } catch (UnsupportedJwtException ex) {
            throw new ResponseStatusException(
                    ErrorStatus.TOKEN_UNSUPPORTED.getHttpStatus(),
                    ErrorStatus.TOKEN_UNSUPPORTED.getMessage()
            );

        } catch (SignatureException ex) {
            throw new ResponseStatusException(
                    ErrorStatus.TOKEN_SIGNATURE_FAILED.getHttpStatus(),
                    ErrorStatus.TOKEN_SIGNATURE_FAILED.getMessage()
            );

        } catch (JwtException ex) {
            throw new ResponseStatusException(
                    ErrorStatus.TOKEN_INVALID.getHttpStatus(),
                    ErrorStatus.TOKEN_INVALID.getMessage()
            );
        }
    }

    // 토큰으로부터 Authentication 객체 생성
    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();

        Long id = Long.valueOf(claims.getSubject());
        String loginId = claims.get("login_id", String.class);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        UserPrincipalDTO userPrincipalDTO = new UserPrincipalDTO(id, loginId, roles);

        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new).toList();

        return new UsernamePasswordAuthenticationToken(userPrincipalDTO, null, authorities);
    }

}
