package org.example.wowelang_backend.board.dto.image;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MultipartPreSignedUrlResponseDTO {
	private String uploadId;
	private String imageKey;
	private String imageUrl;
	private List<String> preSignedUrls;
}
