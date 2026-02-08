package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import trip.diary.entity.Trip;
import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "여행 목록 조회 응답 DTO")
public class TripDto {

    @Schema(description = "여행 ID", example = "1")
    private Long tripId;

    @Schema(description = "여행 제목", example = "즐거운 서울 여행")
    private String title;       // 제목 (없으면 null)

    @Schema(description = "여행지", example = "서울")
    private String destination; // 여행지

    @Schema(description = "시작 날짜", example = "2024-07-01")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2024-07-05")
    private LocalDate endDate;

    @Schema(description = "여행 상태 (1: 다녀온 여행, 2: 예정된 여행)", example = "1")
    private int status; // 1: 다녀온 여행, 2: 예정된 여행

    @Schema(description = "썸네일 이미지 URL", example = "https://i.imgur.com/bM8yb4v.jpeg")
    private String imageUrl;    // 썸네일 이미지

    @Schema(description = "여행 설명 (Note)", example = "친구들과 함께 갔던 잊지 못할 여름 휴가")
    private String description;

    // Entity -> DTO 변환 메서드
    public static TripDto from(Trip trip) {
        return TripDto.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .destination(trip.getDestination())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .imageUrl(trip.getImageUrl())
                .description(trip.getDescription())
                .build();
    }
}