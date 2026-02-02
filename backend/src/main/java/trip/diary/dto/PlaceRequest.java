package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장소 등록/수정 요청")
public record PlaceRequest(
        @Schema(description = "장소명", example = "에펠탑") String name,
        @Schema(description = "장소 설명") String description,
        @Schema(description = "카테고리", example = "관광지") String category,
        @Schema(description = "대표 이미지 인덱스 (0부터 시작)") Integer coverIndex) {
}
