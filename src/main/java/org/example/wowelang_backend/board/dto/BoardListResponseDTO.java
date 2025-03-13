package org.example.wowelang_backend.board.dto;

import lombok.*;
import org.example.wowelang_backend.board.domain.Board;

@Getter
@AllArgsConstructor
public class BoardListResponseDTO {

    private final Long boardId;

    private final String boardName;

    public static BoardListResponseDTO of(Board board) {
        return new BoardListResponseDTO(board.getId(), board.getBoardName());
    }
}

// builder 패턴을 써야하는지에 대한 고민을 해보자

//public static BoardListResponse of(Board board) {
//    return BoardListResponse.builder()
//            .boardId(board.getId())
//            .boardName("board.getBoardName()")
//            .build();
//}
