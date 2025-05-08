package org.example.wowelang_backend.security;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;


/*
 • UserService(회원가입 로직)과는 역할이 다릅니다.
 •	UserService는 비즈니스 로직(회원 생성·수정·조회 등)을 담당
 •	CustomUserDetailsService는 스프링 시큐리티가 인증 시 사용자 정보를 불러오는 역할
*/
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /*
      로그인 시 AuthenticationManager가 호출할 메서드.
      @param loginId 사용자가 입력한 아이디
      @return 스프링 시큐리티가 사용하는 UserDetails 객체
      @throws UsernameNotFoundException 사용자가 없으면 던짐
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + loginId));

        // 스프링 시큐리티가 이해할 수 있는 User 객체로 변환
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getLoginId())
                .password(user.getPassword())        // DB에 암호화된(encode된) 패스워드
                .roles(user.getUsertype().name())    // ex) ROLE_NATIVE, ROLE_FOREIGN, ROLE_ADMIN
                .build();
    }
}