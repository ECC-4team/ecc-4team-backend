package trip.diary.dto;

public record SuccessResponse<T>(
        boolean success,T data
) {
}
