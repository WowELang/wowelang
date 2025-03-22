package org.example.wowelang_backend.board.service;

import org.example.wowelang_backend.board.domain.Board;
import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.dto.PostDTO;
import org.example.wowelang_backend.board.repository.BoardRepository;
import org.example.wowelang_backend.board.repository.PostRespository;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    @Autowired
    private final PostRespository postRespository;
    private final BoardRepository boardRepository;

    public PostService(PostRespository postRespository, BoardRepository boardRepository) {
        this.postRespository = postRespository;
        this.boardRepository = boardRepository;
    }

    public List<PostDTO> getPostList(Long boardId) throws IllegalArgumentException {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.BOARD_NOT_FOUND.getMessage()));

        List<Post> postList = postRespository.findAllByBoard(board);

        return postList.stream()
                .map(post -> PostDTO.builder()
                        .post(post)
                        .build())
                .toList();
    }
}