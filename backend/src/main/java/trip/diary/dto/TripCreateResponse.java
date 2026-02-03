package trip.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TripCreateResponse {
    private int code;
    private String message;
    private TripIdData data;

    @Getter
    @AllArgsConstructor
    public static class TripIdData {
        private Long tripId;
    }
}