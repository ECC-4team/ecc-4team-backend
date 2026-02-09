package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "API 공통 응답 형")
public class CommonResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 데이터")
    private final T data;

    // 1. 데이터가 있는 성공 응답
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data);
    }

    // 2. 데이터가 없는 성공 응답
    public static CommonResponse<Void> success() {
        return new CommonResponse<>(true, null);
    }
}