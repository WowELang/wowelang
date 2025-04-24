package org.example.wowelang_backend.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.wowelang_backend.board.dto.PostImageResponseDTO;
import org.example.wowelang_backend.board.service.FileService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/file")
public class PostFileController {

    @Autowired
    private final FileService fileService;

    public PostFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ApiResponse<PostImageResponseDTO> uploadFile(HttpServletRequest request,
                                                        @RequestParam("filename") String filename,
                                                        @RequestParam("contentType") String contentType) throws IOException {

        return ApiResponse.onSuccess(fileService.uploadCompressedImage(request.getInputStream(), filename, contentType));

    }
}
