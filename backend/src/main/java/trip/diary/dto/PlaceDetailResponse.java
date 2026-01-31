package trip.diary.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PlaceDetailResponse(
        Long placeId,
        Long tripId,
        String name,
        String description,
        String category,
        String coverImageUrl,
        List<String> imageUrls,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
