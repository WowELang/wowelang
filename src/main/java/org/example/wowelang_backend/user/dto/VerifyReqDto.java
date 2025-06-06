package org.example.wowelang_backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.wowelang_backend.user.domain.Gender;
import org.example.wowelang_backend.user.domain.Usertype;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerifyReqDto {
    private String loginId;
    private String email;
    private String password;
    private String name;
    private LocalDate birthday;
    private String major;
    private Gender gender;
    private Usertype usertype;
    private String country; // Foreign인 경우 사용
    private Integer code;
}
