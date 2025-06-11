package org.example.wowelang_backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.example.wowelang_backend.auth.dto.UserPrincipalDTO;

import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;

import org.example.wowelang_backend.user.domain.User;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.security.Key;

import java.util.Base64;
import java.util.Date;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 유효기간(초단위)
    @Value("${jwt.token-validity-in-seconds}")
    private Long accesstokenValidityInSeconds;

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

      /*액세스 토큰 생성*/
    public String createAccessToken(Long userId, String loginId, List<String> roles){
        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + accesstokenValidityInSeconds * 1000);

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

    /**
     * 리프레시 토큰 생성
     * 리프레시 토큰은 클라이언트와 DB에 모두 저장
      */
    public String createRefreshToken(UserPrincipalDTO userPrincipalDTO, long refreshTtl) {

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTtl);

        String refreshToken = Jwts.builder()
                .setIssuedAt(now)
                    .setExpiration(validity)
                        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                            .compact();

        User user = User.builder()
                .id(userPrincipalDTO.getId())
                    .loginId(userPrincipalDTO.getLoginId()).build();

        // 리프레시 토큰은 db에 저장해둠
        // TODO : RTR방식을 사용해서 db접근이 꽤나 필요할 것으로 예상하므로 추후 redis와 같은 인메모리 세션을 두는 것을 고려
        refreshTokenRepository.save(RefreshToken.of(user, refreshToken, refreshTtl));

        return refreshToken;
    }

    // 액세스 토큰에서 유저 아이디 꺼내오는 용도
    public String getUserId(String token) {
        return Jwts.parser().setSigningKey(getSigningKey())
            .build().parseClaimsJws(token)
            .getBody().getSubject();
    }

    // 헤더에서 액세스 토큰 추출
    public String extractAccessToken(String accessTokenWithBearer) {
        if(accessTokenWithBearer ==null || !accessTokenWithBearer.startsWith("Bearer ")) {
            throw new NullPointerException(ErrorStatus.TOKEN_NOT_EXISTS.getMessage());
        }

        return accessTokenWithBearer.substring(7);
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

        // 사용자의 정보를 담은 Authentication 객체 반환
        // 사용자정보(principal, 비밀번호, 권한 순서)
        return new UsernamePasswordAuthenticationToken(userPrincipalDTO, null, authorities);
    }

}
