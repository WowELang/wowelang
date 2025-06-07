package org.example.wowelang_backend.board.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.wowelang_backend.common.BaseEntity;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Column(name = "image_key", unique = true)
    private String imageKey;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_posted")
    private Boolean isPosted;

    // Stream 업로드는 관련 X
    @Column(name = "s3_upload_id")
    private String s3UploadId;

    // Stream 업로드는 관련 X
    @Column(name = "is_s3_uploaded")
    private Boolean isS3Uploaded;

    public Image(String imageKey, String imageUrl, String s3UploadId, Boolean isPosted, Boolean isS3Uploaded) {
        this.imageKey = imageKey;
        this.imageUrl = imageUrl;
        this.s3UploadId = s3UploadId;
        this.isPosted = isPosted;
        this.isS3Uploaded = isS3Uploaded;
    }

    public void updateIsS3Uploaded(boolean flag) {
        this.isS3Uploaded = flag;
    }
}
