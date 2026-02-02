package trip.diary.global.image;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    // 과하지 않게 "이미지만" 허용 (필요하면 늘려)
    private static final Set<String> ALLOWED_EXT = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB (원하면 줄여)

    /**
     * 로컬 저장 후, DB에 저장할 이미지 URL 경로를 반환한다.
     * 예: /images/uuid.png
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("이미지 파일이 너무 큽니다. (최대 10MB)");
        }

        // 1) 원본 파일명에서 확장자 추출
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = getExtension(original);

        // 2) 확장자 화이트리스트 검증 (간단/실용)
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, webp)");
        }

        // 3) 저장 디렉토리 준비
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 폴더 생성 실패", e);
        }

        // 4) UUID 파일명으로 저장
        String storedName = UUID.randomUUID() + ext;
        Path target = dir.resolve(storedName).normalize();

        // 혹시라도 경로 탈출 방지
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }

        // 5) DB에 저장할 URL 반환
        return "/images/" + storedName;
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf(".");
        if (dot < 0) return "";
        return filename.substring(dot).toLowerCase();
    }
}
