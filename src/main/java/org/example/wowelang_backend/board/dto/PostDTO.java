package org.example.wowelang_backend.board.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.wowelang_backend.board.domain.Post;

import java.time.LocalDateTime;

@Getter
public class PostDTO {

    private final Long id;
    private final String title;
    private final String content;
    private final Long replyCnt;
    private final Long views;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    @Builder
    public PostDTO(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.replyCnt = post.getReplyCnt();
        this.views = post.getViews();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
    }

}
