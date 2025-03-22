package org.example.wowelang_backend.board.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.domain.Reply;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class FreeBoardPostListDTO {

    private final Long id;
    private final String title;
    private final String content;
    private final Long replyCnt;
    private final Long views;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public FreeBoardPostListDTO(Long id, String title, String content, Long replyCnt, Long views,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = id;
        this.title = title;
        this.content = content;
        this.replyCnt = replyCnt;
        this.views = views;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

//    public static FreeBoardPostListDTO of (Post post) {
//        return FreeBoardPostListDTO.builder()
//                .id(post.getId())
//                .title(post.getTitle())
//                .content(post.getContent())
//                .replyCnt(post.getReplyCnt())
//                .views(post.getViews())
//                .createdAt()
//                .updatedAt(post.getUpdatedAt())
//                .build();
//    }
}