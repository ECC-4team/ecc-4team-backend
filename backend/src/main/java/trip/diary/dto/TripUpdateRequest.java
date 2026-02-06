package trip.diary.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripUpdateRequest {

    @Size(max = 50, message = "여행 제목은 50자 이내여야 합니다.")
    private String title;

    // PATCH에서는 값을 안 보낼 수도 있으므로 @NotBlank 삭제 (Null 허용)
    @Size(max = 100, message = "여행지는 100자 이내여야 합니다.")
    private String destination;

    private LocalDate startDate;
    private LocalDate endDate;
    private String imageUrl;
    private String description;
}