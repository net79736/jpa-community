package com.jpacommunity.board.core.entity;

import com.jpacommunity.board.api.dto.AttachmentRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_attachments")
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;          // 게시물 ID

    @Column(name = "original_filename")
    private String originalFilename;      // 원본 파일 이름

    @Column(name = "saved_filename")
    private String savedFilename;      // 저장 파일 이름
    private String filepath;      // 파일 경로
    private Long size;        // 파일 크기

    private Boolean hasThumbnail;        // Thumbnail 존재 유무

    public Attachment(AttachmentRequest attachmentRequest) {
        this.originalFilename = attachmentRequest.getFilename();
        // this.filepath = attachmentRequest.getFilepath();
        this.size = attachmentRequest.getSize();
    }

    public void updatePost(Post post) {
        this.post = post;
    }

    public void updateSavedFilename(String savedFilename) {
        this.savedFilename = savedFilename;
    }

    public void updatePath(String path) {
        this.filepath = path;
    }

    public void updateThumbnail(boolean thumbnail) {
        this.hasThumbnail = thumbnail;
    }
}
