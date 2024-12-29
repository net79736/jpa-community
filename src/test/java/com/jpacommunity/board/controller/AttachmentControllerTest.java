package com.jpacommunity.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpacommunity.board.api.dto.PostCreateRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SqlGroup({
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/category-insert-date.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD),
})
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper om;

    // END 는 자동 완성 후 커서 위치
    @Test
    @Disabled
    public void file_upload_test() throws Exception {
        // given
        File file1 = new File("/Users/ijong-ug/업로드 테스트 파일/1590461435359_28129.gif");
        File file2 = new File("/Users/ijong-ug/업로드 테스트 파일/1590461682637.jpg");
        File file3 = new File("/Users/ijong-ug/업로드 테스트 파일/가라오케_사장_노래모음.m4a");
        File file4 = new File("/Users/ijong-ug/업로드 테스트 파일/reservation-service-test-data.sql");

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .categoryId(1L)
                .title("hihi~~")
                .content("content body")
                .build();

        String requestBody = om.writeValueAsString(postCreateRequest);

        MockMultipartFile m1 = convertToMultipartFile(file1);
        MockMultipartFile m2 = convertToMultipartFile(file2);
        MockMultipartFile m3 = convertToMultipartFile(file3);
        MockMultipartFile m4 = convertToMultipartFile(file4);

        List files = List.of(m1, m2, m3, m4);

        // 로그인 요청으로 리프레시 토큰 발급
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/attachments")
                    .file(m4)
                    .file(m3)
                    .file(m2)
                    .file(m1)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
        ).andExpect(status().isOk());
        // then
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    public void file_upload_test_v2() throws Exception {
        // given
        File file1 = new File("/Users/ijong-ug/업로드 테스트 파일/1590461435359_28129.gif");
        File file2 = new File("/Users/ijong-ug/업로드 테스트 파일/1590461682637.jpg");
        File file3 = new File("/Users/ijong-ug/업로드 테스트 파일/가라오케_사장_노래모음.m4a");
        File file4 = new File("/Users/ijong-ug/업로드 테스트 파일/reservation-service-test-data.sql");

        PostCreateRequest postCreateRequest = PostCreateRequest.builder()
                .categoryId(1L)
                .title("hihi 제목을 넣어요")
                .content("content body")
                .build();

        String requestBody = om.writeValueAsString(postCreateRequest);
        System.out.println(requestBody);

        MockMultipartFile m1 = convertToMultipartFile(file1);
        MockMultipartFile m2 = convertToMultipartFile(file2);
        MockMultipartFile m3 = convertToMultipartFile(file3);
        MockMultipartFile m4 = convertToMultipartFile(file4);

        // 로그인 요청으로 리프레시 토큰 발급
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/posts")
                .file(m1) // 파일 추가
                .file(m2)
                .file(m3)
                .file(m4)
                .part(new MockPart("postCreateRequest", requestBody.getBytes(StandardCharsets.UTF_8)))
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE) // 멀티파트 요청 설정
        ).andExpect(status().isOk());

        // then
    }


    // END 는 자동 완성 후 커서 위치
    @Test
    public void upload_test() throws Exception {
        // given
        File file4 = new File("/Users/ijong-ug/업로드 테스트 파일/reservation-service-test-data.sql");
        MockMultipartFile m4 = convertToMultipartFile(file4);

        byte[] fileData = m4.getBytes();

        File target = new File("/Users/ijong-ug/Documents/GitHub/server/src/main/resources/static/upload", "test.jql");
        FileCopyUtils.copy(fileData, target);
    }

    private static MockMultipartFile convertToMultipartFile(File file) throws IOException {
        try (InputStream input = new FileInputStream(file)) {
            byte[] bytes = toByteArray(input);
            return new MockMultipartFile(
                    "file",                          // 파라미터 이름
                    file.getName(),                  // 파일 이름
                    Files.probeContentType(file.toPath()), // MIME 타입
                    bytes                           // 파일 데이터
            );
        }
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096]; // 4KB 버퍼
        int bytesRead;
        while ((bytesRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    // END 는 자동 완성 후 커서 위치
    @Test
    public void tmp_dir_create_test() throws Exception {
        // VM 옵션 확인
        String playHiveHome = System.getProperty("playhive.home");
        System.out.println("VM 옵션 - playhive.home: " + playHiveHome);
        playHiveHome = playHiveHome == null ? "/Users/ijong-ug/Documents/GitHub/server/src/main/resources/static/upload" : playHiveHome;

        // 파일 경로 확인 및 생성 테스트
        File tempDir = new File(playHiveHome, "temp-folder");
        if (!tempDir.exists()) {
            if (tempDir.mkdirs()) {
                System.out.println("임시 디렉토리를 생성했습니다: " + tempDir.getAbsolutePath());
            } else {
                System.out.println("임시 디렉토리 생성에 실패했습니다: " + tempDir.getAbsolutePath());
            }
        } else {
            System.out.println("이미 디렉토리가 존재합니다: " + tempDir.getAbsolutePath());
        }
    }

}