package org.example.wowelang_backend.board.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.User;
import org.example.wowelang_backend.UserRepository;
import org.example.wowelang_backend.board.domain.Board;
import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.dto.PostCreateRequestDTO;
import org.example.wowelang_backend.board.dto.PostResponseDTO;
import org.example.wowelang_backend.board.repository.BoardRepository;
import org.example.wowelang_backend.board.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    @Transactional
    public void createPost(PostCreateRequestDTO postCreateRequestDTO, Long boardId) {

        User user = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("임시 오류입니다 유저가 존재하지 않습니다."));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("임시 오류입니다.게시판이 존재하지 않습니다."));

        // boardId, userId, content, title 넣어줘야함
        // boardId는 pathVariable로 받고, userId는 인증 과정에서 토큰에서 Id 가져오는걸로
        Post post = new Post(postCreateRequestDTO.getTitle(), postCreateRequestDTO.getContent(), board, user);

        postRepository.save(post);
    }

    @Transactional
    public PostResponseDTO getPost(Long boardId, Long postId) {

        return postRepository.findById(postId).stream()
    }
}

