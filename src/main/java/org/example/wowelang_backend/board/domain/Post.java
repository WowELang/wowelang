package org.example.wowelang_backend.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.wowelang_backend.User;
import org.example.wowelang_backend.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    @OneToMany(mappedBy = "post")
    private List<Reply> reply;

    public Post(String title, String content, Board board, User user ) {
        this.title = title;
        this.content = content;
        this.board = board;
        this.user = user;
    }
}
