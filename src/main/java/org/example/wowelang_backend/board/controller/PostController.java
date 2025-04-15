package org.example.wowelang_backend.board.controller;

import org.example.wowelang_backend.board.domain.Post;
import org.example.wowelang_backend.board.dto.PageResponseDTO;
import org.example.wowelang_backend.board.dto.PostCreateDTO;
import org.example.wowelang_backend.board.dto.PostDTO;
import org.example.wowelang_backend.board.service.PostService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.naming.NotContextException;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResponseDTO<PostDTO>> getPostList(@RequestParam Long boardId, @PageableDefault(page = 0, size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) throws Exception {

        return ApiResponse.onSuccess(postService.getPostList(boardId, pageable));
    }

    @PostMapping("")
    public ApiResponse<Long> createPost(@RequestParam Long boardId,
                                        @RequestBody PostCreateDTO postCreateDto) {

        return ApiResponse.created(postService.createPost(boardId, postCreateDto));
    }
}