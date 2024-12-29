package com.jpacommunity.board.api.controller.response;

import com.jpacommunity.board.core.entity.Attachment;

public class AttachmentResponse {
    private Long id;
    private Long postId;          // 게시물 ID
    private String filename;      // 파일 이름
    private String filepath;      // 파일 경로
    private Long size;        // 파일 크기

    public AttachmentResponse(Long id, Long postId, String filename, String filepath, Long size) {
        this.id = id;
        this.postId = postId;
        this.filename = filename;
        this.filepath = filepath;
        this.size = size;
    }

    public AttachmentResponse(Attachment attachment) {
        this.id = attachment.getId();
        this.postId = attachment.getPost().getId();
        // this.filename = attachment.getFilename();
        this.filepath = attachment.getFilepath();
        this.size = attachment.getSize();
    }

    public void update() {
        this.id = 99L;
        this.filename = "kimchi";
        this.postId = 3L;
        this.filepath = "kimchi";
        this.size = 22223L;
    }
}
