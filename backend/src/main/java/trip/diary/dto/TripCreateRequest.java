package trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "여행 생성 요청 DTO")
public class TripCreateRequest {

    @Schema(description = "여행 제목 (선택)", example = "즐거운 서울 여행")
    @Size(max = 50, message = "여행 제목은 50자 이내여야 합니다.")
    private String title;

    @Schema(description = "여행지", example = "서울")
    @NotBlank(message = "여행 장소를 입력해주세요.")
    @Size(max = 100, message = "여행 장소는 100자 이내여야 합니다.")
    private String destination;

    @Schema(description = "국내/해외 구분(국내이면 true / 해외면 false)", example = "true")
    private Boolean isDomestic;

    @Schema(description = "여행 시작 날짜", example = "2024-07-01")
    @NotNull(message = "시작 날짜를 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Schema(description = "여행 종료 날짜", example = "2024-07-05")
    @NotNull(message = "종료 날짜를 입력해주세요.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Schema(description = "여행 이미지 (선택)", example = "https://i.imgur.com/bM8yb4v.jpeg")
    private String imageUrl;

    @Schema(description = "여행 설명 (선택)", example = "친구들과 함께 가는 여름 휴가!")
    private String description;
}