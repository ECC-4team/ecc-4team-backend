package trip.diary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripUpdateRequest {
    private String title;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;
    private String note; // DB의 description으로 매핑
}