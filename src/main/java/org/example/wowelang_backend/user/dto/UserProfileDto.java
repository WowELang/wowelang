package org.example.wowelang_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.wowelang_backend.user.domain.Usertype;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long userId;
    private String nickname;
    private CharacterInfoDto character;
    private List<InterestDto> interests;
    private String name; // 이름
    private String major; // 전공
    private Usertype usertype; // 유저타입
    private String country; // 국적
}
