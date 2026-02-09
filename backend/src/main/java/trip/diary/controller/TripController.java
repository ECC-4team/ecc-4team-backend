package trip.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.*;
import trip.diary.service.TripService;

import java.util.List;
import java.util.Map;

@Tag(name = "여행 메인 홈 API", description = "여행 생성, 목록 조회, 상세 조회, 수정, 삭제")
@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // 여행 생성
    @Operation(summary = "여행 생성", description = "새로운 여행을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "여행 생성 성공",
                    content = @Content(schema = @Schema(implementation = CommonResponse.class))),

            // ▼▼▼ 400 에러: 클라이언트가 잘못된 데이터를 보낸 경우 ▼▼▼
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "1. 날짜 로직 오류",
                                            summary = "종료일이 시작일보다 빠름",
                                            value = """
                                                    {
                                                      "message": "여행 종료일은 시작일보다 빠를 수 없습니다."
                                                    }
                                                    """),
                                    @ExampleObject(name = "2. 필수 입력값 누락",
                                            summary = "여행지, 시작일, 종료일 중 누락 발생",
                                            value = """
                                                    {
                                                      "message": "여행 장소를 입력해주세요."
                                                    }
                                                    """),
                                    @ExampleObject(name = "3. 글자 수 초과",
                                            summary = "제목(50자), 여행지(100자) 초과",
                                            value = """
                                                    {
                                                      "message": "여행 제목은 50자 이내여야 합니다."
                                                    }
                                                    """)
                            })),

            // ▼▼▼ 404 에러: 리소스를 찾을 수 없는 경우 ▼▼▼
            @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 사용자",
                                    summary = "토큰은 유효하나 DB에 유저가 없음",
                                    value = """
                                            {
                                              "message": "존재하지 않는 사용자입니다."
                                            }
                                            """)
                    ))
    })
    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Long>>> createTrip(
            @RequestBody @Valid TripCreateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long tripId = tripService.createTrip(request, userDetails.getUsername());

        // { success: true, data: { "tripId": 1 } }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(Map.of("tripId", tripId)));
    }

    // 여행 목록 조회
    @Operation(summary = "여행 목록 조회", description = "로그인한 사용자의 모든 여행을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TripDto.class))),

            // ▼▼▼ 409 에러: 사용자 정보 충돌 (요청하신 부분) ▼▼▼
            @ApiResponse(responseCode = "409", description = "서버 상태와 충돌",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 사용자",
                                    summary = "토큰은 유효하나 DB에 유저 정보 없음",
                                    value = """
                                            {
                                              "message": "존재하지 않는 사용자입니다."
                                            }
                                            """)
                    ))
    })
    @GetMapping
    public ResponseEntity<CommonResponse<List<TripDto>>> getTrips(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        List<TripDto> trips = tripService.getTrips(userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success(trips));
    }

    // 여행 상세 조회
    @Operation(summary = "여행 상세 조회", description = "특정 여행의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 여행을 찾을 수 없음")
    })
    @GetMapping("/{tripId}")
    public ResponseEntity<CommonResponse<TripDetailDto>> getTripDetail(
            @Parameter(description = "여행 ID", example = "1") @PathVariable Long tripId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        TripDetailDto data = tripService.getTripDetail(tripId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success(data));
    }

    // 여행 수정
    @Operation(summary = "여행 수정", description = "여행 정보를 수정합니다.")
    @PatchMapping("/{tripId}")
    public ResponseEntity<CommonResponse<Void>> updateTrip(
            @Parameter(description = "여행 ID", example = "1") @PathVariable Long tripId,
            @Valid @RequestBody TripUpdateRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        tripService.updateTrip(tripId, request, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success());
    }

    // 여행 삭제
    @Operation(summary = "여행 삭제", description = "특정 여행을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (본인 여행 아님)")
    })
    @DeleteMapping("/{tripId}")
    public ResponseEntity<CommonResponse<Void>> deleteTrip(
            @Parameter(description = "여행 ID", example = "1") @PathVariable Long tripId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        tripService.deleteTrip(tripId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success());
    }
}