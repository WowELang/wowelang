package org.example.wowelang_backend.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.wowelang_backend.board.dto.image.MultipartPreSignedUrlRequestDTO;
import org.example.wowelang_backend.board.dto.image.MultipartPreSignedUrlResponseDTO;
import org.example.wowelang_backend.board.dto.image.MultipartUploadCompleteRequestDTO;
import org.example.wowelang_backend.board.dto.image.PostImageResponseDTO;
import org.example.wowelang_backend.board.service.ImageService;
import org.example.wowelang_backend.common.apiPayLoad.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class PostImageController {

    private final ImageService fileService;

    @PostMapping("/upload")
    public ApiResponse<PostImageResponseDTO> uploadFile(HttpServletRequest request,
                                                        @RequestParam("filename") String filename,
                                                        @RequestParam("contentType") String contentType) throws IOException {

        return ApiResponse.onSuccess(fileService.uploadStreamImage(request.getInputStream(), request.getContentLengthLong(), filename, contentType));
    }

    @PostMapping("/upload-url")
    public ApiResponse<MultipartPreSignedUrlResponseDTO> getMultipartPreSignedUrls(@RequestBody MultipartPreSignedUrlRequestDTO requestDTO){

        MultipartPreSignedUrlResponseDTO response = fileService.initiateMultipartUpload(
            requestDTO.getFilename(), requestDTO.getContentType(), requestDTO.getPartCount()
        );

        return ApiResponse.onSuccess(response);
    }

    @PostMapping("/upload-url-complete")
    public ApiResponse<String> completeMultipartUpload(@RequestBody MultipartUploadCompleteRequestDTO requestDTO) {
        fileService.completeMultipartUpload(requestDTO);
        return ApiResponse.onSuccess("업로드 완료");
    }
}