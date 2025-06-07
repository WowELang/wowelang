package org.example.wowelang_backend.board.dto.image;

import java.util.List;

import lombok.Getter;

@Getter
public class MultipartUploadCompleteRequestDTO {
	private String imageKey;
	private String uploadId;
	private List<PartETagDTO> partETagS;
}
