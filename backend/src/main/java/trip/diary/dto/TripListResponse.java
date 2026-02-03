package trip.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class TripListResponse {
    private int code;
    private String message;
    private List<TripDto> data;
}