package org.example.wowelang_backend.board.dto.image;

import lombok.Getter;

@Getter
public class MultipartPreSignedUrlRequestDTO {
	private String filename;
	private String contentType;
	private Long partCount;
}
