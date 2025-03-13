package org.example.wowelang_backend.board.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.board.dto.BoardListResponseDTO;
import org.example.wowelang_backend.board.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    @Transactional
    public List<BoardListResponseDTO> getBoardLists() {

        return boardRepository.findAll().stream()
                .map(BoardListResponseDTO::of)
                .toList();
    }
}
