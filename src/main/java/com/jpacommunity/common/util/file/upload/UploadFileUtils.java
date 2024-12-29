package com.jpacommunity.common.util.file.upload;

import com.jpacommunity.global.exception.JpaCommunityException;
import com.jpacommunity.common.util.file.MediaUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static com.jpacommunity.global.exception.ErrorCode.INVALID_TYPE;

public class UploadFileUtils {
    private static final Logger logger = LoggerFactory.getLogger(UploadFileUtils.class);
    public static final String UPLOAD_PATH = "/Users/ijong-ug/Documents/GitHub/server/src/main/resources/static/upload"; // 업로드 경로
    // public static final String UPLOAD_PATH = new File("src/main/resources/static").getAbsolutePath(); // 업로드 경로

    public static List<String> uploadFile(String uploadPath, String detPath, String originalName, byte[] fileData) throws Exception {
        String uploadFolder = Paths.get(uploadPath, detPath).toString();

        makeDir(uploadFolder); // 폴더 생성

        logger.info("upload() called uploadPath: {}", uploadPath);
        logger.info("upload() called uploadFolder: {}", uploadFolder);
        // logger.info("upload() called fileData: {}", fileData);

        UUID uid = UUID.randomUUID();

        //	uid 와 filename 을 합친 파일 이름
        String savedName = uid + "_" + originalName;

        // 업로드할 디렉토리 생성
        String savedPath = calcPath(uploadPath);
        File target = new File(savedPath, savedName);

        logger.info("target path : {}", target.getAbsolutePath());
        logger.info("target path : {}", target.getPath());
        logger.info("target name : {}", target.getName());

        // 임시 디렉토리에 업로드 된 파일을 지정된 디렉토리로 복사
        // 파일 업로드는 이미 완료 됨(뒤에서 썸네일 생성할 것임)

        FileCopyUtils.copy(fileData, target);

        // 썸네일 생성 여부 로직 분리
        String uploadedThumbnailFileName = handleThumbnailGeneration(originalName, savedPath, savedName);

        // 파일이 존재하는 경우 삭제
        // if (target.exists()) {
        //	    target.delete();
        // }
        String [] strArr = {savedPath, originalName, savedName, uploadedThumbnailFileName};
        return Arrays.asList(strArr);
    }

    /**
     * 썸네일 생성 여부를 판단하고 생성합니다.
     *
     * @param originalName 원본 파일 이름
     * @param savedPath 저장 경로
     * @param savedName 저장된 파일 이름
     * @return 생성된 썸네일 파일 이름 또는 빈 문자열
     * @throws Exception 썸네일 생성 중 오류
     */
    private static String handleThumbnailGeneration(String originalName, String savedPath, String savedName) throws Exception {
        String formatName = extractFileExtension(originalName);

        if (MediaUtils.getMediaType(formatName) != null) {
            logger.info("썸네일 파일을 생성 가능한 파일입니다.");
            return createThumbnail(savedPath, savedName);
        }
        return "";
    }

    /**
     * 파일 확장자를 추출합니다.
     *
     * @param fileName 파일 이름
     * @return 확장자 (e.g., "jpg", "png")
     */
    private static String extractFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    // 폴더 생성기
    private static void makeDir(String...paths) {
        // 폴더가 이미 존재한다면 함수 종료
        if (new File(paths[paths.length - 1]).exists()) {
            return;
        }
        for (String path : paths) {;
            System.out.println(path);
            File dirPath = new File(path);
            if (!dirPath.exists()) {
                dirPath.mkdir(); // 디렉토리 생성
            }
        }
    }

    //	Calendar Path 를 생성한다 (uploadpath/2019/08/23/ ~.jpg )
    private static String calcPath(String uploadPath) {
        Calendar cal = Calendar.getInstance();
        String yearPath = uploadPath + File.separator + cal.get(Calendar.YEAR);
        // monthpath 는 yearpath 를 합친 것
        String monthPath = yearPath + File.separator
                + new DecimalFormat("00").format(cal.get(Calendar.MONTH) + 1);
        // datepath 는 monthpath 를 합친 것
        String datePath = monthPath + File.separator
                + new DecimalFormat("00").format(cal.get(Calendar.DATE));
        makeDir(uploadPath,
                yearPath, monthPath, datePath);
        logger.info(datePath);
        return datePath;
    }

    private static String createThumbnail(String path, String fileName) throws Exception{
        logger.info("createThumbnail path: {}, fileName: {}", path, fileName);

        // 이미지를 읽기 위한 버퍼
        BufferedImage sourceImg =
                ImageIO.read(new File(path, fileName));

        if (sourceImg == null) {
            throw new JpaCommunityException(INVALID_TYPE, "Invalid image file: " + fileName);
        }

        // 100 픽셀 단위의 썸네일 생성
        BufferedImage destImg =
                Scalr.resize(sourceImg, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_HEIGHT, 100);
        // 썸네일의 이름
        String thumbnailName = "s_" + fileName;
        File thumbnailFile = new File(path, thumbnailName);

        // 확장자 이름으로 생각 됨
        String formatName = extractFileExtension(fileName);
        // 썸네일 생성
        ImageIO.write(destImg, formatName.toUpperCase(), thumbnailFile);
        logger.info("썸네일이 성공적으로 생성되었습니다. thumbnailName: {}", thumbnailFile.getAbsolutePath());
        // 썸네일의 이름을 리턴함
        return thumbnailName.replace(File.pathSeparatorChar, '/');
    }
}
