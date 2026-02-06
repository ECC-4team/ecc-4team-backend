package trip.diary.dto;

import lombok.Builder;
import lombok.Getter;
import trip.diary.entity.Trip;

import java.time.LocalDate;

@Getter
@Builder
public class TripDetailDto {
    private Long tripId;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private int status;       // 1: 완료, 2: 예정
    private String imageUrl;
    private String note;

    public static TripDetailDto from(Trip trip) {
        return TripDetailDto.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .destination(trip.getDestination())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .imageUrl(trip.getImageUrl())
                .note(trip.getDescription()) // Entity의 description -> DTO의 note로 매핑
                .build();
    }
}