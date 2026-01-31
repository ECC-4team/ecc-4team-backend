package trip.diary.dto;

import java.time.LocalDateTime;

public record PlaceListResponse(
        Long placeId,
        Long tripId,
        String name,
        String description,
        String category,
        String coverImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
