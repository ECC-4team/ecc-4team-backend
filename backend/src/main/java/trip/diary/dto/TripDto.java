package trip.diary.dto;

import lombok.Builder;
import lombok.Getter;
import trip.diary.entity.Trip;
import java.time.LocalDate;

@Getter
@Builder
public class TripDto {
    private Long tripId;
    private String title;       // 제목 (없으면 null)
    private String destination; // 여행지
    private LocalDate startDate;
    private LocalDate endDate;
    private int status; // 1: 다녀온 여행, 2: 예정된 여행
    private String imageUrl;    // 썸네일 이미지

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
                .build();
    }
}