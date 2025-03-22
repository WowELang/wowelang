package org.example.wowelang_backend.board.controller;

import org.example.wowelang_backend.board.service.PostService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

//    @GetMapping("/")
//    public ApiResponse<> getFreeBoardPostList() {
//
//        return ApiResponse.onSuccess(postService.)
//    }
}