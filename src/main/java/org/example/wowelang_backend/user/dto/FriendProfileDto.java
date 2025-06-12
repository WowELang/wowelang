package org.example.wowelang_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FriendProfileDto {
    private CharacterInfoDto character;
    private String nickname;
    private List<InterestDto> interests;
    private String countryOrMajor;
}
