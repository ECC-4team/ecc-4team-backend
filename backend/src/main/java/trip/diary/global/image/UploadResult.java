package trip.diary.global.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UploadResult {
    private String url;
    private String storageKey; // public_id
}