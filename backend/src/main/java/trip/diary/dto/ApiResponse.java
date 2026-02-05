package trip.diary.dto;

public record ApiResponse<T>(boolean success, T data) {

    // 성공 시 데이터와 함께 반환 (200 OK)
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    // 데이터 없이 성공만 반환할 때
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null);
    }
}