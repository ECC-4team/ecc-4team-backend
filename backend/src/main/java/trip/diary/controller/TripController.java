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
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Tag(name = "여행 메인 홈 API", description = "여행 생성, 목록 조회, 상세 조회, 수정, 삭제")
@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final ObjectMapper objectMapper;
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
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<Long>> createTrip(

            @Parameter(description = "여행 정보 JSON (String)", required = true)
            @RequestPart("data") String data,

            @Parameter(description = "대표 이미지 파일", required = false)
            @RequestPart(value = "image", required = false) MultipartFile image,

            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {

        // 1. String -> 객체 변환 (수동 파싱)
        TripCreateRequest request = objectMapper.readValue(data, TripCreateRequest.class);

        // 2. 유효성 검사 (@Valid가 동작하지 않으므로 수동 검사 필요, 혹은 Service에서 처리)
        // (간단하게 필수값인 destination 체크 예시)
        if (request.getDestination() == null || request.getDestination().isBlank()) {
            throw new IllegalArgumentException("여행 장소를 입력해주세요.");
        }

        // 3. 서비스 호출
        Long tripId = tripService.createTrip(request, image, userDetails.getUsername());

        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.success(tripId));
    }

    // 여행 목록 조회
    @Operation(summary = "여행 목록 조회", description = "로그인한 사용자의 모든 여행을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TripDto.class))),

            // ▼▼▼ 404 에러: 사용자 정보 충돌 (요청하신 부분) ▼▼▼
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
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
            @ApiResponse(responseCode = "400", description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "남의 여행 조회",
                                    summary = "내 토큰으로 다른 사람의 여행 요청",
                                    value = """
                                            {
                                              "message": "조회 권한이 없습니다."
                                            }
                                            """)
                    )),
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "없는 여행 ID",
                                    summary = "DB에 존재하지 않는 ID 요청",
                                    value = """
                                            {
                                              "message": "존재하지 않는 여행입니다."
                                            }
                                            """)
                    ))
    })
    @GetMapping("/{tripId}")
    public ResponseEntity<CommonResponse<TripDetailDto>> getTripDetail(
            @Parameter(description = "여행 ID", example = "1") @PathVariable Long tripId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        TripDetailDto data = tripService.getTripDetail(tripId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success(data));
    }

    // 여행 수정
    @Operation(summary = "여행 수정", description = "여행 정보를 수정합니다. (수정할 필드만 보내면 됩니다)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (권한, 날짜, 유효성)",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "1. 권한 없음",
                                            summary = "남의 여행 수정 시도",
                                            value = """
                                                    {
                                                      "message": "수정 권한이 없습니다."
                                                    }
                                                    """),
                                    @ExampleObject(name = "2. 필수값 누락",
                                            summary = "필수 데이터 누락 (@Valid)",
                                            value = """
                                                    {
                                                      "message": "여행지를 입력해주세요."
                                                    }
                                                    """)
                            })),

            // ▼▼▼ 404 에러: 여행 없음 ▼▼▼
            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 여행 ID",
                                    summary = "DB에 없는 ID로 수정 요청",
                                    value = """
                                            {
                                              "message": "존재하지 않는 여행입니다."
                                            }
                                            """)
                    ))
    })
    @PatchMapping(value = "/{tripId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<TripDetailDto>> updateTrip(
            @Parameter(description = "여행 ID", example = "1") @PathVariable Long tripId,

            @Parameter(description = "수정할 정보 JSON (String)", required = true)
            @RequestPart("data") String data,

            @Parameter(description = "변경할 이미지 파일 (없으면 기존 이미지 유지)", required = false)
            @RequestPart(value = "image", required = false) MultipartFile image,

            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) throws JsonProcessingException {

        // 1. JSON String -> DTO 변환
        TripUpdateRequest request = objectMapper.readValue(data, TripUpdateRequest.class);

        // 2. 서비스 호출 (이미지 파일도 같이 넘김)
        TripDetailDto updatedTrip = tripService.updateTrip(tripId, request, image, userDetails.getUsername());

        return ResponseEntity.ok(CommonResponse.success(updatedTrip));
    }

    // 여행 삭제
    @Operation(summary = "여행 삭제", description = "특정 여행을 삭제합니다. (관련된 장소, 일정도 모두 삭제됩니다)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "삭제 권한 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "남의 여행 삭제",
                                    summary = "본인 여행이 아닌 경우",
                                    value = """
                                            {
                                              "message": "삭제 권한이 없습니다."
                                            }
                                            """)
                    )),

            @ApiResponse(responseCode = "404", description = "여행을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 여행 ID",
                                    summary = "DB에 없는 ID로 삭제 요청",
                                    value = """
                                            {
                                              "message": "존재하지 않는 여행입니다."
                                            }
                                            """)
                    ))
    })
    @DeleteMapping("/{tripId}")
    public ResponseEntity<CommonResponse<Void>> deleteTrip(
            @Parameter(description = "여행 ID", example = "1") @PathVariable Long tripId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        tripService.deleteTrip(tripId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success());
    }
}