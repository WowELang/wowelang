package org.example.wowelang_backend.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.example.wowelang_backend.board.domain.Post;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostResponseDTO {

    private Long postId;

    private String title;

    private String content;

    private Long replyCnt;

    private Boolean isDelete;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static PostResponseDTO of(Post post) {
        return PostResponseDTO.builder()
    }
}
