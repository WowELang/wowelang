package org.example.wowelang_backend.board.repository;

import org.example.wowelang_backend.board.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Modifying
    @Query("UPDATE Image i SET i.isPosted = true WHERE i.imageKey IN :keys")
    void updatePostedTrueByKeys(@Param("keys") List<String> keys);

    List<Image> findByIsPostedFalse();

    Optional<Image> findByImageKeyAndS3UploadId(String imageKey, String s3UploadId);

    // S3 업로드 실패 또는 미완료된 이미지 (스케줄러용)
    List<Image> findByIsPostedFalseAndIsS3UploadedFalse();

}
