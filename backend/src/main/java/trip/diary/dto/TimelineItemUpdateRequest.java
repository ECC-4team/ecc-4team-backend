package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "타임라인 아이템 수정 요청")
public record TimelineItemUpdateRequest(
        @Schema(description = "일정 날짜(TripDay 기준)", example = "2026-02-22")
        LocalDate dayDate,
        @Schema(description = "시작 시간 (30분 단위)", example = "10:00")
        LocalTime startTime,
        @Schema(description = "종료 시간 (30분 단위)", example = "11:30")
        LocalTime endTime,
        @Schema(description = "장소 ID", example = "5")
        Long placeId
) {}