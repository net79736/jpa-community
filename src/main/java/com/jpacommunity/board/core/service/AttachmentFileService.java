package com.jpacommunity.board.core.service;

import com.jpacommunity.board.api.controller.response.AttachmentResponse;
import com.jpacommunity.board.api.dto.AttachmentRequest;
import com.jpacommunity.board.core.entity.Attachment;
import com.jpacommunity.board.core.entity.Post;
import com.jpacommunity.board.core.repository.AttachmentJpaRepository;
import com.jpacommunity.board.core.repository.PostJpaRepository;
import com.jpacommunity.global.exception.JpaCommunityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.*;

import static com.jpacommunity.common.util.file.upload.UploadFileUtils.UPLOAD_PATH;
import static com.jpacommunity.common.util.file.upload.UploadFileUtils.uploadFile;
import static com.jpacommunity.global.exception.ErrorCode.RESOURCE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentFileService {

    private final AttachmentJpaRepository attachmentJpaRepository;
    private final PostJpaRepository postJpaRepository;

    @Deprecated
    public AttachmentResponse create(List<MultipartFile> files) throws Exception {
        log.info("파일을 업로드 할 예정입니다.");
        for (MultipartFile file : files) {
            System.out.println("Uploaded file name: " + file.getOriginalFilename());
            uploadFile(UPLOAD_PATH, "", Objects.requireNonNull(file.getOriginalFilename()), file.getBytes());
        }
        return null;
    }

    @Transactional
    public List<Attachment> create(List<MultipartFile> files, List<AttachmentRequest> attachmentRequests) throws Exception {
        log.info("파일 업로드를 시작합니다. attachmentRequests Number: {}", attachmentRequests.size());

        // 파일 이름과 저장 경로를 매핑하는 맵
        Map<String, String> uploadedFilePaths = new HashMap<>();
        // 파일 이름과 UUID 파일 이름를 매핑하는 맵
        Map<String, String> uploadedFileUuidNames = new HashMap<>();
        // 파일 이름과 썸네일 파일 이름를 매핑하는 맵
        Map<String, String> thumbnailFileNames = new HashMap<>();

        // 파일 업로드 수행
        manageFileUpload(files, uploadedFilePaths, uploadedFileUuidNames, thumbnailFileNames);

        // 업로드 파일 메타 데이터 생성 및 저장
        List<Attachment> attachments = saveAttachments(attachmentRequests, uploadedFilePaths, uploadedFileUuidNames, thumbnailFileNames);

        log.info("파일 업로드 및 첨부파일 저장 작업이 완료되었습니다.");
        return attachments;
    }

    /**
     * 파일 목록을 순회하며 업로드 한다.
     *
     * @param files 업로드 파일 목록
     * @param pathMap 파일 이름과 저장 경로를 매핑하는 맵
     * @param fileMap 파일 이름과 UUID 파일 이름를 매핑하는 맵
     * @param thumbNailMap 파일 이름과 썸네일 파일 이름를 매핑하는 맵
     * @throws Exception
     */
    private void manageFileUpload(
            List<MultipartFile> files,
            Map<String, String> pathMap,
            Map<String, String> fileMap,
            Map<String, String> thumbNailMap
    ) throws Exception {
        for (MultipartFile file : files) {
            String originalFilename = Normalizer.normalize(Objects.requireNonNull(file.getOriginalFilename()), Normalizer.Form.NFC);
            if (originalFilename == null || originalFilename.isEmpty()) {
                log.warn("파일 이름이 비어있습니다. 업로드를 건너뜁니다.");
                continue;
            }

            log.info("업로드 중인 파일: {}", originalFilename);

            // 파일 업로드
            List<String> uploadedFileInfo = uploadFile(UPLOAD_PATH, "", originalFilename, file.getBytes());

            String filePath = uploadedFileInfo.get(0); // 오리지날 파일 이름
            String fileName = uploadedFileInfo.get(1); // 파일 업로드 경로
            String savedName = uploadedFileInfo.get(2); // UUID 파일 이름
            String thumbnailName = uploadedFileInfo.get(3); // 썸네일 파일 이름

            // 업로드된 파일 정보
            // 파일 이름: /Users/ijong-ug/Documents/GitHub/jpa-community/src/main/resources/static/upload/2024/12/29
            // 파일 경로: 1590461435359_28129.gif
            // UUID 파일 이름: a7c79277-479d-4ad2-9917-32d0561b6520_1590461435359_28129.gif
            log.info("업로드된 파일 정보 - 파일 이름: {}, 파일 경로: {}, UUID 파일 이름: {}, 썸네일 파일 이름: {}", filePath, fileName, savedName, thumbnailName);

            // 파일 정보를 Map 에 추가하는 메서드
            updateFileMaps(pathMap, fileMap, thumbNailMap, originalFilename, filePath, savedName, thumbnailName);
        }
    }

    /**
     * 파일 정보를 Map에 추가하는 메서드
     *
     * @param pathMap 파일 이름과 저장 경로를 매핑하는 맵
     * @param fileMap 파일 이름과 UUID 파일 이름를 매핑하는 맵
     * @param thumbNailMap 파일 이름과 썸네일 파일 이름를 매핑하는 맵
     * @param originalFilename 파일 원본 이름
     * @param filePath 파일 경로
     * @param savedName UUID 파일 이름
     * @param thumbnailName 썸네일 파일 이름
     */
    private void updateFileMaps(
            Map<String, String> pathMap,
            Map<String, String> fileMap,
            Map<String, String> thumbNailMap,
            String originalFilename,
            String filePath,
            String savedName,
            String thumbnailName
    ) {
        // pathMap.put(originalFilename, filePath);
        // fileMap.put(originalFilename, savedName);
        // thumbNailMap.put(originalFilename, thumbnailName);
        pathMap.put(originalFilename, filePath);
        fileMap.put(originalFilename, savedName);
        thumbNailMap.put(originalFilename, thumbnailName);
    }

    private List<Attachment> saveAttachments(
            List<AttachmentRequest> attachmentRequests,
            Map<String, String> uploadedFilePaths,
            Map<String, String> uploadedFileUuidNames,
            Map<String, String> thumbnailFileNames
    ) {
        List<Attachment> attachments = new ArrayList<>();

        for (AttachmentRequest attachmentRequest : attachmentRequests) {
            // Attachment 객체 생성
            Attachment attachment = prepareAttachment(attachmentRequest, uploadedFilePaths, uploadedFileUuidNames, thumbnailFileNames);

            // 연관된 Post 객체 가져오기
            Long postId = attachmentRequest.getPostId();
            Post post = getById(postId);
            attachment.updatePost(post);

            // Attachment 저장
            Attachment savedAttachment = attachmentJpaRepository.save(attachment);
            attachments.add(savedAttachment);

            log.info("Attachment 저장 완료. 파일 이름: {}, 경로: {}", attachment.getOriginalFilename(), attachment.getFilepath());
        }
        return attachments;
    }

    /**
     * 첨부파일 객체 생성 및 경로 설정
     *
     * @param attachmentRequest     첨부파일 요청 객체
     * @param uploadedFilePaths     업로드된 파일 경로 맵
     * @param uploadedFileUuidNames 업로드된 파일 UUID 이름 맵
     * @param thumbnailFileNames    업로드된 썸네일 파일 이름 맵
     * @return 생성된 Attachment 객체
     */
    private static Attachment prepareAttachment(
            AttachmentRequest attachmentRequest,
            Map<String, String> uploadedFilePaths,
            Map<String, String> uploadedFileUuidNames,
            Map<String, String> thumbnailFileNames) {
        Attachment attachment = new Attachment(attachmentRequest);

        // 업로드된 파일 경로 설정
        String savedPath = uploadedFilePaths.get(attachment.getOriginalFilename());
        if (StringUtils.isEmpty(savedPath)) {
            log.warn("파일 경로를 찾을 수 없습니다. 원본 파일 이름: {}", attachment.getOriginalFilename());
            return null;
        }
        attachment.updatePath(savedPath);

        // 업로드된 파일 이름 설정
        String savedName = uploadedFileUuidNames.get(attachment.getOriginalFilename());
        if (StringUtils.isEmpty(savedName)) {
            log.warn("업로드된 파일 이름을 찾을 수 없습니다. 원본 파일 이름: {}", attachment.getOriginalFilename());
            return null;
        }
        attachment.updateSavedFilename(savedName);

        // 썸네일 유무 설정
        String thumbnailName = thumbnailFileNames.get(attachment.getOriginalFilename());
        if (StringUtils.isEmpty(thumbnailName)) {
            log.warn("업로드된 썸네일 이름을 찾을 수 없습니다. 원본 파일 이름: {}", attachment.getOriginalFilename());
            attachment.updateThumbnail(false);
        } else {
            attachment.updateThumbnail(true);
        }

        return attachment;
    }

    /**
     * Post 객체를 ID로 조회
     * @param postId 게시물 ID
     * @return 조회된 Post 객체
     */
    private Post getById(Long postId) {
        return postJpaRepository.findById(postId)
                .orElseThrow(() -> new JpaCommunityException(RESOURCE_NOT_FOUND, "해당 ID의 Post 를 찾을 수 없습니다. ID: " + postId));
    }

    public List<AttachmentRequest> generateAttachmentRequests(Long postId, List<MultipartFile> files) {
        List<AttachmentRequest> attachmentRequests = new ArrayList<>();

        if (postId == null) {
            log.error("게시글 아이디가 존재하지 않습니다.");
            throw new JpaCommunityException(RESOURCE_NOT_FOUND);
        }

        if (!files.isEmpty()) {
            for (MultipartFile file : files) {
                attachmentRequests.add(
                        AttachmentRequest.builder()
                                .postId(postId)
                                .filename(Normalizer.normalize(Objects.requireNonNull(file.getOriginalFilename()), Normalizer.Form.NFC))
                                .size(file.getSize())
                                .build()
                );
            }
        }

        return attachmentRequests;
    }

    @Transactional
    public void delete(Long id) {

    }
}
