package trip.diary.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TimelineDto {
    // POST 요청: 일정(아이템) 추가
    public record TimelineItemCreateRequest(
            LocalDate dayDate,
            LocalTime startTime,
            LocalTime endTime,
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
