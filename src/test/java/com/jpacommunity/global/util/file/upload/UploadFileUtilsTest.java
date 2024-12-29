package com.jpacommunity.global.util.file.upload;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Paths;

import static com.jpacommunity.common.util.file.upload.UploadFileUtils.UPLOAD_PATH;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class UploadFileUtilsTest {

    // END 는 자동 완성 후 커서 위치
    @Test
    @DisplayName("Paths.get 을 사용하여 업로드 경로 조합시 결과 테스트")
    public void upload_path_print_test() throws Exception {
        // given
        // when
        String imagesFolder = Paths.get(UPLOAD_PATH, "/baseball", "/community", "/유머").toString();
        System.out.println(imagesFolder);

        // then
        assertThat(imagesFolder).isEqualTo(UPLOAD_PATH + "/baseball/community/유머");
    }

}