package org.example.wowelang_backend.board.controller;

import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.dto.PostCreateDTO;
import org.example.wowelang_backend.board.dto.PostDTO;
import org.example.wowelang_backend.board.service.PostService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("")
    public ApiResponse<List<PostDTO>> getPostList(@RequestParam Long boardId) {

        return ApiResponse.onSuccess(postService.getPostList(boardId));
    }

    @PostMapping("")
    public ApiResponse<Long> createPost(@RequestParam Long boardId,
                                        @RequestBody PostCreateDTO postCreateDto) {

        return ApiResponse.created(postService.createPost(boardId, postCreateDto));
    }
}