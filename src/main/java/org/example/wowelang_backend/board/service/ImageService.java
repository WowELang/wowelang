package org.example.wowelang_backend.board.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.wowelang_backend.board.domain.Image;
import org.example.wowelang_backend.board.dto.image.MultipartPreSignedUrlResponseDTO;
import org.example.wowelang_backend.board.dto.image.MultipartUploadCompleteRequestDTO;
import org.example.wowelang_backend.board.dto.image.PostImageResponseDTO;
import org.example.wowelang_backend.board.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // Stream 업로드 방식
    @Transactional
    public PostImageResponseDTO uploadStreamImage(InputStream inputStream, Long contentLengthLong, String filename, String contentType) throws IOException {
        try {

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

    // aws Multipart + Pre-Signed URL 방식
    @Transactional
    public MultipartPreSignedUrlResponseDTO initiateMultipartUpload(String filename, String contentType, Long partCount) {
        try {
            String extension = getExtension(contentType);

            String uuid = UUID.randomUUID().toString();

            String key = "tmp/" + uuid + "_" + filename + extension;

            InitiateMultipartUploadResult initiateResult = amazonS3.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(bucket, key)
                    .withObjectMetadata(new ObjectMetadata())
            );

            String uploadId = initiateResult.getUploadId();

            String imageUrl = amazonS3.getUrl(bucket, key).toString();

            Image image = Image.builder()
                .imageKey(key)
                .imageUrl(imageUrl)
                .s3UploadId(uploadId)
                .isPosted(false)
                .isS3Uploaded(false)
                .build();

            imageRepository.save(image);

            List<String> preSignedUrls = new ArrayList<>();

            Date expiration = new Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000 * 60 * 10; // 10분 유효 (클라이언트가 모든 파트를 업로드할 시간)
            expiration.setTime(expTimeMillis);

            for (int i = 1; i <= partCount; i++) {
                GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.PUT) // PUT 메소드로 업로드 가능하도록
                        .withExpiration(expiration);

                generatePresignedUrlRequest.addRequestParameter("uploadId", uploadId); // 업로드 ID 파라미터 추가
                generatePresignedUrlRequest.addRequestParameter("partNumber", String.valueOf(i)); // 파트 번호 파라미터 추가

                URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
                preSignedUrls.add(url.toString());
            }

            return new MultipartPreSignedUrlResponseDTO(uploadId, key, imageUrl, preSignedUrls);
        } catch (Exception e) {
            log.error("Multipart Pre-Signed URL 생성 중 에러 발생", e);
            throw new RuntimeException("Multipart Pre-Signed URL 생성 중 에러 발생");
        }
    }

    // aws Multipart + Pre-Signed URL 방식
    @Transactional
    public void completeMultipartUpload(MultipartUploadCompleteRequestDTO requestDTO) {
        try {
            imageRepository.findByImageKeyAndS3UploadId(requestDTO.getImageKey(), requestDTO.getUploadId())
                .ifPresentOrElse(image -> {
                    List<PartETag> partETags = requestDTO.getPartETagS().stream()
                        .map(dto -> new PartETag(dto.getPartNumber(), dto.getEtag()))
                        .sorted(Comparator.comparingInt(PartETag::getPartNumber))
                        .collect(Collectors.toList());

                    CompleteMultipartUploadRequest completeRequest =
                        new CompleteMultipartUploadRequest(bucket, requestDTO.getImageKey(), requestDTO.getUploadId(),
                            partETags);
                    amazonS3.completeMultipartUpload(completeRequest);

                    image.updateIsS3Uploaded(true);
                }, () -> {
                    abortMultipartUpload(requestDTO.getImageKey(), requestDTO.getUploadId());
                });
        } catch (Exception e) {
            log.error("Multipart Upload 완료 처리 중 에러 발생", e);
            throw new RuntimeException("Multipart Upload 완료 처리 중 에러 발생");
        }
    }

    private void abortMultipartUpload(String imageKey, String uploadId) {
        try {
            amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, imageKey, uploadId));
            log.info("S3 Multipart Upload 취소 성공: Key - {}, UploadId - {}", imageKey, uploadId);
        } catch (Exception e) {
            log.error("S3 Multipart Upload 취소 실패: Key - {}, UploadId - {}", imageKey, uploadId, e);
        }
    }

    // 이미지 확장자 설정 메서드
    private String getExtension(String contentType) {
		return switch (contentType) {
			case "image/jpeg", "image/jpg" -> ".jpg";
			case "image/png" -> ".png";
			case "image/gif" -> ".gif";
			default -> throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + contentType);
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

        // Stream업로드는 isS3Uploaded와 관련이 없음
        if(!unusedImages.isEmpty()) {
            for( Image image : unusedImages) {
                try {
                    amazonS3.deleteObject(bucket, image.getImageKey());
                } catch (Exception e) {
                    log.error("S3 삭제 실패 (스케줄러): {}", image.getImageKey(), e);
                }
                imageRepository.delete(image);
            }
        }

        // isPosted=false 이고 isS3Uploaded=false 이며, 일정 시간 이상 경과된 이미지 처리
        // (Pre-Signed URL만 발급되고 S3 업로드가 완료되지 않은 경우)
        List<Image> pendingOrFailedUploads = imageRepository.findByIsPostedFalseAndIsS3UploadedFalse();
        if(!pendingOrFailedUploads.isEmpty()) {
            log.warn("S3 Multipart 업로드 완료되지 않은 이미지 발견 (DB에서만 삭제): {}개", pendingOrFailedUploads.size());
            for (Image image : pendingOrFailedUploads) {
                // S3에 파트가 남아있을 수 있으므로 Multipart Upload를 취소
                abortMultipartUpload(image.getImageKey(), image.getS3UploadId());
                imageRepository.delete(image);
            }
        }
    }
}
