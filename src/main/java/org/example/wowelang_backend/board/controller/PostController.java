package org.example.wowelang_backend.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.board.dto.PostCreateRequestDTO;
import org.example.wowelang_backend.board.dto.PostResponseDTO;
import org.example.wowelang_backend.board.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/{boardId}")
    public ResponseEntity<Void> createPost(@PathVariable Long boardId,
                                           @RequestBody PostCreateRequestDTO postCreateRequestDTO) {

        postService.createPost(postCreateRequestDTO, boardId);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<>> getPostList(@PathVariable Long boardId) {

        postService.getPostList(boardId);

        return ResponseEntity
    }

    @GetMapping("/{boardId}/{postId}")
    public ResponseEntity<PostResponseDTO> getPost(@PathVariable Long boardId,
                                                   @PathVariable Long postId) {

        return ResponseEntity.ok().body(postService.getPost(boardId, postId));
    }
}
