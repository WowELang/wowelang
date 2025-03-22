package org.example.wowelang_backend.board.controller;

import org.example.wowelang_backend.board.dto.PostDTO;
import org.example.wowelang_backend.board.service.PostService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}