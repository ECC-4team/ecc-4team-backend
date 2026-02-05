package trip.diary.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final T data;

    // 1. 데이터가 있는 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    // 2. 데이터가 없는 성공 응답
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null);
    }
}