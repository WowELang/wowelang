package org.example.wowelang_backend.security.custom;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;


/*
 • UserService(회원가입 로직)과는 역할이 다릅니다.
 •	UserService는 비즈니스 로직(회원 생성·수정·조회 등)을 담당
 •	CustomUserDetailsService는 스프링 시큐리티가 인증 시 사용자 정보를 불러오는 역할
*/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // 3) CustomUserDetailsService에서 로그인 아이디 → CustomUserDetails 반환
    @Override
    public UserDetails loadUserByUsername(String loginId) {
        User u = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException(loginId));
        return new CustomUserDetails(
                u.getId(),
                u.getLoginId(),
                u.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_"+u.getUsertype()))
        );
    }

    // JWT 필터 등에서 “PK(subject)” 로 로드할 때 쓸 수 있는 별도 메서드
    // 토큰 검증 등 ID(pk)로 바로 조회가 필요할 때 사용하는 메서드
    public CustomUserDetails loadById(Long id) throws UsernameNotFoundException {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        return toCustomUserDetails(u);
    }

    // User → CustomUserDetails 변환 공통 로직
    private CustomUserDetails toCustomUserDetails(User u) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + u.getUsertype().name())
        );
        return new CustomUserDetails(
                u.getId(),           // PK
                u.getLoginId(),      // 로그인ID
                u.getPassword(),     // 암호화된 패스워드
                authorities          // 권한 리스트
        );
    }
}