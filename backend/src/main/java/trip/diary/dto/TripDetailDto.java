package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import trip.diary.entity.Trip;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "여행 상세 조회 응답 DTO")
public class TripDetailDto {

    @Schema(description = "여행 ID", example = "1")
    private Long tripId;

    @Schema(description = "여행 제목", example = "즐거운 서울 여행")
    private String title;

    @Schema(description = "여행지", example = "서울")
    private String destination;

    @Schema(description = "국내/해외 여부 (true: 국내, false: 해외)", example = "true")
    private Boolean isDomestic;

    @Schema(description = "시작 날짜", example = "2024-07-01")
    private LocalDate startDate;

    @Schema(description = "종료 날짜", example = "2024-07-05")
    private LocalDate endDate;

    @Schema(description = "여행 상태 (1: 다녀온 여행, 2: 예정된 여행)", example = "1")
    private int status;       // 1: 완료, 2: 예정

    @Schema(description = "대표 이미지 URL", example = "https://i.imgur.com/5eDmhnp.jpeg")
    private String imageUrl;

    @Schema(description = "여행 설명 (Note)", example = "친구들과 함께 갔던 잊지 못할 여름 휴가")
    private String description;

    public static TripDetailDto from(Trip trip) {
        return TripDetailDto.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .destination(trip.getDestination())
                .isDomestic(trip.getIsDomestic())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .imageUrl(trip.getImageUrl())
                .description(trip.getDescription())
                .build();
    }
}