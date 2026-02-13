package trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimelineDto {
    // POST 요청: 일정(아이템) 추가
    public record TimelineItemCreateRequest(

            @Schema(description = "일자 (yyyy-MM-dd)", example = "2026-02-13", type = "string", format = "date")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate dayDate,

            @Schema(description = "시작 시간 (HH:mm)", example = "01:00", type = "string", format = "time")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime startTime,

            @Schema(description = "종료 시간 (HH:mm)", example = "02:00", type = "string", format = "time")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime endTime,

            @Schema(description = "장소 ID", example = "10")
            Long placeId
    ) {}

    // POST 응답
    public record TimelineItemCreateResponse(
            Long timelineId
    ) {}

    // GET 응답: 아이템
    public record TimelineItemResponse(
            Long timelineId,
            LocalTime startTime,
            LocalTime endTime,
            Long placeId,
            String placeName
    ) {}

    // GET 응답: day 묶음
    public record TripDayTimelineResponse(
            Long dayId,
            LocalDate dayDate,
            Integer dayIndex,
            String themeTitle,
            String dayNote,
            Integer budgetPlanned,
            Integer budgetSpent,
            List<TimelineItemResponse> items
    ) {}

    // GET 전체 응답
    public record TimelineListResponse(
            List<TripDayTimelineResponse> days
    ) {}
}
