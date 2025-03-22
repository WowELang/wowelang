package org.example.wowelang_backend.user.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "users")
@AllArgsConstructor //빌더 어노테이션 사용시 필요
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본생성자는 jpa에서 필요로하는 경우가 많음
public class User {

    @Id // Id가 두 종류인데 각각 차이가 뭘까요?
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true) // DB에는 무슨 일이 벌어져요??
    private String loginId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate birthday; // 왜 Date가 아니라 LocalDate? - @Temporal 없이 자동매핑, 시간정보 필요없음

    @Column(nullable = false)
    private String major;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Usertype usertype;

    // 재학생 튜터의 경우 이메일 인증 여부 플래그
    private Boolean isEmailVerified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private KoreanTutorAttribute koreanTutorAttribute;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = true)
    private ForeignTuteeAttribute foreignTuteeAttribute;

    @PrePersist // 새로운 엔티티가 db에 처음 저장되기 전에 실행
    public void prePersist() { //엔티티 생성 시점 기록
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate // 기존 엔티티가 db에 업데이트 되기 전 실행
    public void preUpdate() { //마지막으로 수정된 시간 기록
        this.updatedAt = LocalDateTime.now();
    }
}
