package org.example.wowelang_backend.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.board.dto.BoardListResponseDTO;
import org.example.wowelang_backend.board.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;



    @GetMapping("/list")
    public ResponseEntity<List<BoardListResponseDTO>> getBoardLists() {

        return ResponseEntity.ok().body(boardService.getBoardLists());
    }

}
