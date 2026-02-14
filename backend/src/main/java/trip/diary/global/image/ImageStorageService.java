package trip.diary.global.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageStorageService {

    private final Cloudinary cloudinary;

    /**
     * Cloudinary 업로드 후, DB에 저장할 이미지 URL(secure_url)을 반환
     */
    public String upload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 이미지가 없습니다.");
        }

        String publicId = "places/" + UUID.randomUUID(); // 폴더명은 마음대로 조정 가능

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image"
                    )
            );

            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Cloudinary 업로드 실패", e);
        }
    }

    /*

    // 삭제까지 하고 싶으면 public_id 저장해야 함
    public void deleteByPublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary 삭제 실패", e);
        }
    }

     */
}
