package trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TripCreateRequest {

    @Size(max = 50, message = "여행 제목은 50자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "여행 장소를 입력해주세요.")
    @Size(max = 100, message = "여행 장소는 100자 이내여야 합니다.")
    private String destination;

    @NotNull(message = "시작 날짜를 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜를 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String imageUrl;

    private String description;
}