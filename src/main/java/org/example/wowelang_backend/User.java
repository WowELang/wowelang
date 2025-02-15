package org.example.wowelang_backend;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity // 이 애노테이션이 왜 필요해??
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id // Id가 두 종류인데 각각 차이가 뭘까요?
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true) // DB에는 무슨 일이 벌어져요??
    private String loginId;

    @Column(nullable = false)
    private String password;

    private String username;

    private LocalDate birthday; // 왜 Date가 아니라 LocalDate? - @Temporal 없이 자동매핑, 시간정보 필요없음

    @Enumerated(EnumType.ORDINAL) // ORDINAL 과 STRING 차이 비교해보기
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Usertype usertype;

    private String major;

    @Enumerated(EnumType.STRING)
    private Country country;

    private String interest;

    @OneToOne(mappedBy = "user")
    private KoreanTutorAttribute koreanTutorAttribute;

    // 단방향 매핑 우선

//    @OneToMany(mappedBy = "user")
//    private List<Post> post = new ArrayList<>();

//    @OneToMany(mappedBy = "user")
//    private List<Reply> reply = new ArrayList<>();
}
