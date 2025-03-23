package org.example.wowelang_backend.common.apiPayLoad.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.wowelang_backend.common.apiPayLoad.BaseCode;
import org.example.wowelang_backend.common.apiPayLoad.GlobalResponseDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseCode {

    // 본인이 담당한 부분에 맞게 에러코드를 작성해주세요

    // 인증관련 에러
    TOKEN_INVALID(HttpStatus.BAD_REQUEST, "토큰이 유효하지 않습니다."),

    // 게시글 관련 에러
    POST_NOT_CREATED(HttpStatus.BAD_REQUEST, "제목과 내용을 입력해주세요"),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시글을 찾을 수 없습니다."),
    POST_NO_CONTENT(HttpStatus.NO_CONTENT, "게시글이 존재하지 않습니다."),
    POST_NO_MORE(HttpStatus.NOT_FOUND, "게시글 페이지네이션의 끝입니다."),

    //게시판 관련 에러
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 게시판을 찾을 수 없습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public GlobalResponseDTO getGlobalResponse() {
        return GlobalResponseDTO.builder()
                .isSuccess(false)
                .httpStatus(httpStatus)
                .message(message)
                .build();
    }
}
