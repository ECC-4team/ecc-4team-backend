package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Schema(description = "TripDay 일괄 수정 요청")
public class TripDayBulkUpdateRequest {

    @Schema(description = "수정할 TripDay 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<TripDayUpdateItem> days;

    @Getter
    @Setter
    @Schema(description = "TripDay 수정 항목")
    public static class TripDayUpdateItem {
        @Schema(description = "TripDay ID", example = "11", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long dayId;
        @Schema(description = "해당 날짜의 테마/제목", example = "1일차 감성투어")
        private String themeTitle;
        @Schema(description = "해당 날짜 메모", example = "메모")
        private String dayNote;
        @Schema(description = "예산(계획)", example = "50000")
        private Integer budgetPlanned;
        @Schema(description = "예산(실제 지출)", example = "42000")
        private Integer budgetSpent;
    }
}
