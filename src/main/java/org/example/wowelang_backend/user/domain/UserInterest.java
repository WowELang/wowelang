package org.example.wowelang_backend.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//유저와 관심사 간 조인 엔티티
public class UserInterest {

    @EmbeddedId
    private UserInterestId id;

    @ManyToOne(fetch = LAZY)
    @MapsId("userId")
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = LAZY)
    @MapsId("interestId")
    @JoinColumn(name="interest_id")
    private Interest interest;

    public UserInterest(User user, Interest interest) {
        this.user = user;
        this.interest = interest;
        // ID를 직접 초기화
        this.id = new UserInterestId(user.getId(), interest.getId());
    }
}
