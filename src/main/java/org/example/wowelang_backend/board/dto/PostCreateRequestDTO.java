package org.example.wowelang_backend.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PostCreateRequestDTO {

    private String title;

    private String content;
}
