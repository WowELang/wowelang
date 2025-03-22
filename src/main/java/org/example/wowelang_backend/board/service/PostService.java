package org.example.wowelang_backend.board.service;

import org.example.wowelang_backend.User;
import org.example.wowelang_backend.UserRepository;
import org.example.wowelang_backend.board.domain.Board;
import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.dto.PostCreateDTO;
import org.example.wowelang_backend.board.dto.PostDTO;
import org.example.wowelang_backend.board.repository.BoardRepository;
import org.example.wowelang_backend.board.repository.PostRepository;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private final PostRepository postRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<PostDTO> getPostList(Long boardId) throws IllegalArgumentException {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.BOARD_NOT_FOUND.getMessage()));

        return postRepository.findAllByBoard(board).stream()
                .map(PostDTO::from)
                .toList();
    }

    @Transactional
    public Long createPost(Long boardId, PostCreateDTO postCreateDto) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.BOARD_NOT_FOUND.getMessage()));

        // TODO: 인증 로직과 합친 후 변경 예정
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException(("등록되지 않은 유저입니다.")));

        Post post = Post.create(postCreateDto, user, board);

        postRepository.save(post);
        return post.getId();
    }
}