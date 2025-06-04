package org.example.wowelang_backend.board.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.example.wowelang_backend.board.domain.Image;
import org.example.wowelang_backend.board.dto.PostImageResponseDTO;
import org.example.wowelang_backend.board.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public PostImageResponseDTO uploadCompressedImage(InputStream inputStream, Long contentLengthLong, String filename, String contentType) throws IOException {
        try {
            // ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
            //
            // Thumbnails.of(inputStream)
            //         .size(1024, 1024)
            //         .outputQuality(1.0)
            //         .outputFormat("jpg")
            //         .toOutputStream(compressedOut);
            //
            // byte[] compressedImage = compressedOut.toByteArray();
            // InputStream compressedInputStream = new ByteArrayInputStream(compressedImage);

            String extension = getExtension(contentType);
            String uuid = UUID.randomUUID().toString();

            // 파일 임시 업로드
            String key = "tmp/" + uuid + "_" + filename + extension;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(contentLengthLong);

            try {
                amazonS3.putObject(new PutObjectRequest(bucket, key, inputStream, metadata));
            } catch (Exception e) {
                throw new RuntimeException("S3 업로드 중 에러 발생");
            }

            String url = amazonS3.getUrl(bucket, key).toString();

            imageRepository.save(Image.builder()
                    .imageKey(key)
                    .imageUrl(url)
                    .isPosted(false)
                    .build());

            return new PostImageResponseDTO(key, url);
        } catch (Exception e) {
            throw new RuntimeException("이미지 업로드 중 에러 발생");
        }
    }

    // 확장자 설정 메서드
    private String getExtension(String contentType) {
		return switch (contentType) {
			case "image/jpeg", "image/jpg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/gif" -> ".gif";
			default -> "";
		};
    }

    @Transactional
    public void markImageAsPosted(List<String> imageKeyList) {
        imageRepository.updatePostedTrueByKeys(imageKeyList);

    }

    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void deleteUnnecessaryImage() {
        List<Image> unusedImages = imageRepository.findByIsPostedFalse();

        if(!unusedImages.isEmpty()) {
            for( Image image : unusedImages) {
                try {
                    amazonS3.deleteObject(bucket, image.getImageKey());
                } catch (Exception e) {
                    System.out.println("S3 삭제 실패: " + image.getImageKey());
                }
                imageRepository.delete(image);
            }
        }
    }
}
