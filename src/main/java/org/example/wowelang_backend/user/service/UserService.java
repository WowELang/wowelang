package org.example.wowelang_backend.user.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.GlobalResponseDTO;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.common.apiPayLoad.status.SuccessStatus;
import org.example.wowelang_backend.user.domain.ForeignTuteeAttribute;
import org.example.wowelang_backend.user.domain.KoreanTutorAttribute;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.domain.Usertype;
import org.example.wowelang_backend.user.dto.UserSignupReqDto;
import org.example.wowelang_backend.user.dto.VerificationDto;
import org.example.wowelang_backend.user.repository.ForeignTuteeRepository;
import org.example.wowelang_backend.user.repository.KoreanTutorRepository;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final KoreanTutorRepository koreanTutorRepository;
    private final ForeignTuteeRepository foreignTuteeRepository;
    //private final PasswordEncoder passwordEncoder;
    private final UnivcertService univcertService;

    //1단계: 기본 정보 입력 후 임시 사용자 생성
    // 기본정보는 저장하지만 이메일 인증은 되지 않은 상태
    public Long createTempUser(UserSignupReqDto dto) {
        //이메일, 로그인 아이디 중복체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalArgumentException("이미 등록된 아이디입니다.");
        }

        //임시 사용자 생성(isEmailVerified 기본값 false)
        User user = User.builder()
                .loginId(dto.getLoginId())
                .email(dto.getEmail())
                //.password(passwordEncoder.encode(dto.getPassword()))
                .password(dto.getPassword())//로그인 구현시 변경
                .name(dto.getName())
                .birthday(dto.getBirthday())
                .major(dto.getMajor())
                .gender(dto.getGender())
                .usertype(dto.getUsertype())
                .isEmailVerified(false)
                .build();
        try {
            userRepository.save(user); // 유저 타입에 따라 외국인이면 바로 가입, 재학생이면 메일인증으로 진행
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 등록된 이메일 또는 아이디입니다.");
        }

        //유저타입별 추가 정보(이후 최종 가입 단계에서 사용)
        if (dto.getUsertype() == Usertype.NATIVE) {
            KoreanTutorAttribute tutor = new KoreanTutorAttribute();
            tutor.setUser(user);
            tutor.setReputation(0L);
            tutor.setFixCount(0L);
            koreanTutorRepository.save(tutor);
        } else if (dto.getUsertype() == Usertype.FOREIGN) {
            ForeignTuteeAttribute tutee = new ForeignTuteeAttribute();
            tutee.setUser(user);
            tutee.setCountry(dto.getCountry());
            foreignTuteeRepository.save(tutee);
        }

        return user.getId();
    }

    // 2단계: 재학생 튜터일 경우, 인증 진행
    public void sendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다"));
        if (user.getUsertype() == Usertype.FOREIGN) {
            //유학생은 메일인증 x
            return;
        } else if (user.getUsertype() == Usertype.NATIVE) {
            boolean mailSent = univcertService.sendCertifyMail(user.getEmail());
            if (!mailSent) {
                throw new IllegalStateException("이미 인증 요청이 완료되었거나 발송에 실패했습니다.");
            }
        }
    }

    /*
    2-1단계: 인증 코드 검증 및 대학 이메일 인증 업데이트
    @param dto 인증 요청 정보를 담은 DTO (userId, univEmail, univName, code)
    */
    public ResponseEntity<String> verifyUnivEmail(VerificationDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다"));
        if (user.getUsertype() != Usertype.NATIVE) {
            return ResponseEntity.badRequest().body("재학생 튜터만 이메일 인증이 필요합니다");
        }

        boolean success;
        success = univcertService.verifyCode(user.getEmail(), dto.getCode());

        if (success) {
            //인증 성공시 인증상태 업데이트, 저장
            user.setIsEmailVerified(true);
            userRepository.save(user);
            return ResponseEntity.ok("인증이 성공했습니다.");
        }else{
            return ResponseEntity.ok("인증이 실패했습니다.");
        }
    }

    //3단계: 최종 회원가입 완료 처리
    public Long completeSignUp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다ㅏ."));

        if ((user.getUsertype() == Usertype.NATIVE) && (!user.getIsEmailVerified())) {
            throw new IllegalStateException("이메일 인증이 완료되지 않았습니다.");
        }

        //이후 추가 로직 구현 가능 ex) 캐릭터 선택..
        return user.getId();
    }

    //인증된 이메일 초기화
    public void clearCertification(String email) {
        try {
            Map<String, Object> resp = univcertService.clear(email);
            boolean success = Boolean.TRUE.equals(resp.get("success"));
            if (!success) {
                throw new IllegalStateException("UnivCert 초기화 실패: " + resp.get("message"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("UnivCert clear API 호출 오류", e);
        }
    }

    //아이디 중복확인
    public GlobalResponseDTO checkLoginId(String loginId) {
        if (userRepository.existsByLoginId(loginId)) {
            return ErrorStatus.LOGINID_DUPLICATE.getGlobalResponse();
        }
        return SuccessStatus.OK.getGlobalResponse();
    }
}