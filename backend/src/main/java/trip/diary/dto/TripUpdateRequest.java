package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "여행 정보 수정 요청 DTO")
public class TripUpdateRequest {

    @Schema(description = "수정할 여행 제목", example = "두쫀쿠 여행 (수정됨)")
    @Size(max = 50, message = "여행 제목은 50자 이내여야 합니다.")
    private String title;

    @Schema(description = "수정할 여행지", example = "수원")
    @Size(max = 100, message = "여행지는 100자 이내여야 합니다.")
    // PATCH에서는 값을 안 보낼 수도 있으므로 @NotBlank 삭제 (Null 허용)
    private String destination;

    @Schema(description = "수정할 시작 날짜", example = "2024-07-02")
    private LocalDate startDate;

    @Schema(description = "수정할 종료 날짜", example = "2024-07-06")
    private LocalDate endDate;

    @Schema(description = "수정할 대표 이미지 URL", example = "https://i.imgur.com/bM8yb4v.jpeg")
    private String imageUrl;

    @Schema(description = "수정할 설명", example = "일정이 변경되어 수정함")
    private String description;
}