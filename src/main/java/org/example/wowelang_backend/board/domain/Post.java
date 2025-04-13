package org.example.wowelang_backend.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.wowelang_backend.board.dto.PostCreateDTO;
import org.example.wowelang_backend.common.BaseEntity;
import org.example.wowelang_backend.user.domain.User;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    private String content;

    @Column(name = "reply_cnt")
    private Long replyCnt = 0L;

    @Column(name = "is_delete")
    private Boolean isDelete = false;

    private Long views = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "post")
    private List<Reply> reply;

    @Builder
    public Post(String title, String content, User user, Board board) {
        this.title = title;
        this.content = content;
        this.replyCnt = 0L;
        this.isDelete = false;
        this.views = 0L;
        this.user = user;
        this.board = board;
        this.reply = new ArrayList<>();
    }

    public static Post create(PostCreateDTO postCreateDto, User user, Board board) {
        return Post.builder()
                .title(postCreateDto.getTitle())
                .content(postCreateDto.getContent())
                .user(user)
                .board(board)
                .build();
    }
}
