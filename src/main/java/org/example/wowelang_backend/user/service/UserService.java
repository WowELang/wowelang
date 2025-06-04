package org.example.wowelang_backend.user.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.user.domain.ForeignTuteeAttribute;
import org.example.wowelang_backend.user.domain.KoreanTutorAttribute;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.domain.Usertype;
import org.example.wowelang_backend.user.dto.CharacterInfoDto;
import org.example.wowelang_backend.user.dto.InterestDto;
import org.example.wowelang_backend.user.dto.UserProfileDto;
import org.example.wowelang_backend.user.dto.UserSignupReqDto;
import org.example.wowelang_backend.user.repository.ForeignTuteeRepository;
import org.example.wowelang_backend.user.repository.KoreanTutorRepository;
import org.example.wowelang_backend.user.repository.UserInterestRepository;
import org.example.wowelang_backend.user.repository.UserRepository;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final KoreanTutorRepository koreanTutorRepository;
    private final ForeignTuteeRepository foreignTuteeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UnivcertService univcertService;
    private final UserInterestRepository userInterestRepository;

    // 1단계: 기본 정보 입력 후 임시 사용자 생성
    // 기본정보는 저장하지만 이메일 인증은 되지 않은 상태
    public Long createTempUser(UserSignupReqDto dto) {
        //이메일, 로그인 아이디 중복체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException(ErrorStatus.DUPLICATE_EMAIL.getMessage());
        }
        if (userRepository.existsByLoginId(dto.getLoginId())) {
            throw new IllegalArgumentException(ErrorStatus.LOGINID_DUPLICATE.getMessage());
        }


        //임시 사용자 생성(isEmailVerified 기본값 false)
        User user = User.builder()
                .loginId(dto.getLoginId())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getName())
                .birthday(dto.getBirthday())
                .major(dto.getMajor())
                .gender(dto.getGender())
                .usertype(dto.getUsertype())
                .isEmailVerified(false)
                .build();

        userRepository.save(user); // 유저 타입에 따라 외국인이면 바로 가입, 재학생이면 메일인증으로 진행

        //유저타입별 추가 정보(이후 최종 가입 단계에서 사용)
        if (dto.getUsertype() == Usertype.NATIVE) {//재학생
            KoreanTutorAttribute tutor = KoreanTutorAttribute.builder()
                    .user(user)
                    .reputation(0L)
                    .fixCount(0L)
                    .build();
            koreanTutorRepository.save(tutor);

        } else if (dto.getUsertype() == Usertype.FOREIGN) {//유학생
            ForeignTuteeAttribute tutee = ForeignTuteeAttribute.builder()
                    .user(user)
                    .country(dto.getCountry())
                    .build();
            foreignTuteeRepository.save(tutee);
        }

        return user.getId();
    }

    // 2단계: 재학생 튜터일 경우, 인증 진행
    public boolean sendVerificationEmail(String email) {
        // 1) 이메일로 사용자 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        ErrorStatus.USER_NOT_FOUND.getMessage()
                ));

        // 2) 유학생(Foreign)은 인증 메일 발송 없이 바로 true 반환
        if (user.getUsertype() == Usertype.FOREIGN) {
            return true;
        }

        // 3) 재학생(Native)이고 아직 인증되지 않은 경우에만 처리
        if (!user.getIsEmailVerified()) {
            if (univcertService.hasCertificationRequest(user.getEmail())) {
                clearCertification(user.getEmail());
            }
            //새 인증 메일 발송
            boolean mailSent = univcertService.sendCertifyMail(user.getEmail());
            if (!mailSent) {
                throw new IllegalStateException(
                        ErrorStatus.CERTIFICATION_MAIL_FAILED.getMessage()
                );
            }
            return true;
        }

        // 4) 이미 인증이 완료된 경우에는 별도 발송 없이 false 반환 (또는 예외 처리)
        throw new IllegalStateException(
                ErrorStatus.EMAIL_ALREADY_VERIFIED.getMessage()
        );
    }

    //3단계: 인증코드 검증 및 가입 완료
    public Long verifyAndCompleteSignUp(Long userId, int code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        if (user.getUsertype() == Usertype.NATIVE) {
            boolean ok = univcertService.verifyCode(user.getEmail(), code);
            if (!ok) {
                throw new IllegalArgumentException(ErrorStatus.CERTIFICATION_CODE_MISMATCH.getMessage());
            }
            user.setIsEmailVerified(true);
            userRepository.save(user);
        }

        // FOREIGN 은 바로 통과
        return user.getId();
    }

    //인증된 이메일 초기화
    public String clearCertification(String email) {
        try {
            Map<String, Object> resp = univcertService.clear(email);
            boolean success = Boolean.TRUE.equals(resp.get("success"));
            if (!success) {
                throw new IllegalStateException(ErrorStatus.UNIVCERT_CLEAR_FAILED.getMessage() + ": " + resp.get("message"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("UnivCert clear API 호출 오류", e);
        }
        return "인증 상태가 초기화되었습니다.";
    }

    //아이디 중복확인
    public void checkLoginId(String loginId) {
        if (userRepository.existsByLoginId(loginId)) {
            throw new IllegalArgumentException(ErrorStatus.LOGINID_DUPLICATE.getMessage());
        }
    }

    // 최초 닉네임 설정
    public String setNickname(Long userId, String nickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        //최초 설정 여부 검사
        if (user.isNicknameInitialized()) {
            throw new IllegalStateException(ErrorStatus.INITIALIZED_NICKNAME.getMessage());
        }

        user.initNickname(nickname);
        userRepository.save(user);

        return user.getNickname();
    }

    // 최초 캐릭터 설정
    public CharacterInfoDto setCharacter(Long userId, int colorId, int maskId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        //최초 설정 여부 검사
        if (user.isCharacterInitialized()) {
            throw new IllegalStateException("이미 캐릭터가 설정되었습니다.");
        }

        user.initCharacter(colorId, maskId);
        userRepository.save(user);

        // 컨트롤러로 보낼 DTO 생성
        return new CharacterInfoDto(user.getColorId(), user.getMaskId());
    }

    // 내 프로필 조회 (PK, 닉네임, 캐릭터, 관심사)
    @Transactional(readOnly = true)
    public UserProfileDto getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        // 관심사 조회
        List<InterestDto> interests = userInterestRepository.findAllByUserId(userId).stream()
                .map(ui -> new InterestDto(ui.getInterest().getId(), ui.getInterest().getName()))
                .toList();
        // 캐릭터 정보
        CharacterInfoDto character = new CharacterInfoDto(
                user.getColorId(),
                user.getMaskId()
        );

        //유저 국적 정보 추가
        String country = null;
        if (user.getUsertype() == Usertype.FOREIGN){
            ForeignTuteeAttribute attr = user.getForeignTuteeAttribute();
            if (attr != null) {
                country = attr.getCountry();
            }
        }
        return new UserProfileDto(
                user.getId(),
                user.getNickname(),
                character,
                interests,
                user.getName(),
                user.getMajor(),
                user.getUsertype(),
                country
        );
    }

    // 닉네임 수정
    public String updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        // 단순 setter 로 덮어쓰기
        user.setNickname(newNickname);
        // 초기화 플래그가 false 면 true 로, true 면 그대로
        if (!user.isNicknameInitialized()) {
            user.setNicknameInitialized(true);
        }
        userRepository.save(user);

        return user.getNickname();
    }

    // 캐릭터 수정 (언제든)
    public CharacterInfoDto updateCharacter(Long userId, int colorId, int maskId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        // 단순 setter 로 덮어쓰기
        user.setColorId(colorId);
        user.setMaskId(maskId);
        if (!user.isCharacterInitialized()) {
            user.setCharacterInitialized(true);
        }
        userRepository.save(user);

        return new CharacterInfoDto(user.getColorId(), user.getMaskId());
    }
}