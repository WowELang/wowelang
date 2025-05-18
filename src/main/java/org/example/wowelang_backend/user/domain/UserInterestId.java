package org.example.wowelang_backend.user.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//유저와 흥미 간 조인 엔티티 id
public class UserInterestId implements Serializable {
    private Long userId;
    private Long interestId;
}
