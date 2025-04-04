package org.example.wowelang_backend.board.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.wowelang_backend.board.domain.Post;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostDTO {

    private Long id;
    private String title;
    private String content;
    private Long replyCnt;
    private Long views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostDTO from(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .replyCnt(post.getReplyCnt())
                .views(post.getViews())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
