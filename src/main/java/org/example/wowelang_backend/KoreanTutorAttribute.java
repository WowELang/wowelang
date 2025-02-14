package org.example.wowelang_backend;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KoreanTutorAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reputation;

    @Column(name = "fix_rate")
    private Long fixRate;

    @OneToOne(mappedBy = "koreanTutorAttribute") // 단방향 매핑인지, 양방향 매핑인지 생각해보기
    private User user;
}
