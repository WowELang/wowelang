package org.example.wowelang_backend.board.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class PostCreateDTO {
    private String title;
    private String content;
    private List<String> imageKeyList;
}
