package trip.diary.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.TimelineDto;
import trip.diary.dto.TimelineItemUpdateRequest;
import trip.diary.dto.TripDayBulkUpdateRequest;
import trip.diary.global.exception.ErrorResponse;
import trip.diary.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequiredArgsConstructor
@RequestMapping("/trips")
@Tag(name = "Timeline", description = "여행 일정(타임라인) API")
public class TripTimelineController {

    private final TimelineService timelineService;


    @Operation(
            summary = "여행 타임라인 조회",
            description = "여행(tripId)에 속한 모든 날짜(day)와 일정(item)을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TimelineDto.TimelineListResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "존재하지 않는 여행"
                    )
            }
    )
    //GET /trips/{tripId}/timeline
    @GetMapping("/{tripId}/timeline")
    public ResponseEntity<TimelineDto.TimelineListResponse> getTimeline(@PathVariable Long tripId){
        TimelineDto.TimelineListResponse response= timelineService.getTimeline(tripId);
        return ResponseEntity.ok(response);
    }

    /*-----------------------------------------------------------------------------------*/


    @Operation(
            summary = "타임라인 아이템 추가",
            description = "특정 날짜(day)에 새로운 일정(타임라인 아이템)을 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TimelineDto.TimelineItemCreateResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "요청 값 오류"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "여행 또는 날짜를 찾을 수 없음"
                    )
            }
    )
    //POST /trips/{tripId}/timeline
    @PostMapping("/{tripId}/timeline")
    public ResponseEntity<TimelineDto.TimelineItemCreateResponse> createTimelineItem(@PathVariable Long tripId, @RequestBody TimelineDto.TimelineItemCreateRequest request){
        Long timelineItemId=timelineService.addTimelineItem(tripId,request);
        return ResponseEntity.ok(new TimelineDto.TimelineItemCreateResponse(timelineItemId));

    }

    /*-----------------------------------------------------------------------------------*/


    @Operation(
            summary = "타임라인 아이템 삭제",
            description = "타임라인 아이템 ID로 일정을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음")
            }
    )
    //DELETE /timeline/{timelineId}
    @DeleteMapping("/timeline/{timelineId}")
    public ResponseEntity<Void> deleteTimelineItem(@PathVariable Long timelineId){
        timelineService.deleteTimelineItem(timelineId);
        return ResponseEntity.ok().build();
    }

    /*-----------------------------------------------------------------------------------*/


    @Operation(
            summary = "TripDay 일괄 수정",
            description = "여행(tripId)에 속한 TripDay들을 dayId 기준으로 themeTitle/dayNote/budgetPlanned/budgetSpent 값을 일괄 업데이트합니다. " +
                    "요청에 포함된 dayId가 해당 trip에 속하지 않거나 존재하지 않으면 실패합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류 (days 비어있음, dayId 누락 등)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "TripDay를 찾을 수 없음(해당 tripId 소속 아님/존재하지 않음)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })

    //PUT /trips/{tripId}/days
    @PutMapping("/{tripId}/days")
    public ResponseEntity<Void> updateTripDays(@PathVariable Long tripId, @RequestBody TripDayBulkUpdateRequest request){
        timelineService.updateTripDays(tripId,request);
        return ResponseEntity.noContent().build();//204
    }

    /*-----------------------------------------------------------------------------------*/

    @Operation(
            summary = "타임라인 아이템 수정",
            description = """
                    특정 여행(tripId)의 타임라인 아이템(timelineId)을 수정합니다.
                    - 같은 날짜(TripDay) 내에서 시간이 겹치면 409 반환
                    - 끝시간=다음 일정 시작시간은 허용(겹침 아님)
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공 (No Content)"),
            @ApiResponse(responseCode = "400", description = "요청값 검증 실패 (시간 형식/시작>=끝 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "대상 리소스 없음 (trip/day/timeline/place not found)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "시간 겹침으로 수정 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })

    //PUT /trips/{tripId}/timeline/{timelineId}
    @PutMapping("/{tripId}/timeline/{timelineId}")
    public ResponseEntity<Void> updateTimelineItem(@PathVariable Long tripId, @PathVariable Long timelineId, @RequestBody TimelineItemUpdateRequest request){
        timelineService.updateTimelineItem(tripId,timelineId,request);
        return ResponseEntity.noContent().build();//204
    }
}
