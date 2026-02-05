package trip.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import trip.diary.entity.Trip;
import java.time.LocalDate;

@Getter
@Builder
public class TripUpdateResponse {
    private Long tripId;
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private int status;
    private String note;

    public static TripUpdateResponse from(Trip trip) {
        return TripUpdateResponse.builder()
                .tripId(trip.getId())
                .title(trip.getTitle())
                .destination(trip.getDestination())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .status(trip.getStatus())
                .note(trip.getDescription())
                .build();
    }
}