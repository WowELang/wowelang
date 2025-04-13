package org.example.wowelang_backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    // Access Token 유효기간(초단위)
    @Value("${jwt.token-validity-in-seconds}")
    private Long tokenValidityInSeconds;


    // Bean 초기화 시점에, secretKey를 Base64로 인코딩하여 사용
    // JWT 라이브러리에서 요구하는 형식이며, 일관된 형식을 유지하기 위함
    @PostConstruct
    protected void init() {
        // secretKey가 null이 아니라면 인코딩 처리합니다.
        if (secretKey != null && !secretKey.isEmpty()) {
            secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        }
    }

    // 서명을 위한 Key 객체를 생성합니다.
    // Keys.hmacShaKeyFor()는 비밀키(byte[])를 받아 Key 객체를 생성합니다.
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

      /*JWT 토큰 생성
      @param userId 사용자 식별자 (예: user의 id 값)
      @param roles  사용자 권한 목록 (userType enum 사용)
      @return 생성된 JWT 토큰 문자열*/


    public String createToken(String userId, List<String> roles){
        // Claims 설정: subject에는 userId, 그리고 roles 추가
        Claims claims = Jwts.claims().setSubject(userId).build();
        claims.put("roles", roles);

        // 현재 시간과 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInSeconds * 1000);

        // 토큰 빌더를 통해 토큰 생성 및 서명
        return Jwts.builder()
                .setClaims(claims)          // Claims 설정
                .setIssuedAt(now)           // 발행 시간
                .setExpiration(validity)    // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // HS256 알고리즘과 Key 객체를 사용해 서명
                .compact();
    }

      /*JWT 토큰의 유효성 검증
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 그렇지 않으면 예외를 던짐*/

    public boolean validateToken(String token) {
        try {
            // 토큰의 서명과 만료 시간을 검증합니다.
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 예외 발생 시 전역 예외 처리에서 처리할 수 있도록 RuntimeException을 던집니다.
            throw new RuntimeException(ErrorStatus.TOKEN_INVALID.getMessage());
        }
    }

      /*JWT 토큰에서 사용자 식별자(subject) 추출
     * @param token JWT 토큰 문자열
     * @return 토큰 내의 subject (userId)
     * @throws RuntimeException 토큰이 유효하지 않으면 ErrorStatus.TOKEN_INVALID 메시지를 포함한 예외 발생*/

    public String getUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException(ErrorStatus.TOKEN_INVALID.getMessage());
        }
    }
}
