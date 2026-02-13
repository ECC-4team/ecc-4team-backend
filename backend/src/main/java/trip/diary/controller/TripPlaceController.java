package trip.diary.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import trip.diary.dto.PlaceRequest;
import trip.diary.dto.PlaceDetailResponse;
import trip.diary.dto.PlaceListResponse;
import trip.diary.dto.PlaceResponse;
import trip.diary.global.exception.ErrorResponse;
import trip.diary.service.TripPlaceService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.List;

@Tag(name = "여행 장소", description = "여행별 장소 CRUD API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/trips/{tripId}/places")
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;
    private final ObjectMapper objectMapper;

    @Operation(
            summary = "장소 목록 조회",
            description = "특정 여행의 모든 장소 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PlaceListResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "여행을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<PlaceListResponse>> getPlaces(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId) {
        List<PlaceListResponse> places = tripPlaceService.getPlaces(tripId);
        return ResponseEntity.ok(places);
    }

    /*-----------------------------------------------------------------------------------------------*/

    @Operation(
            summary = "장소 상세 조회",
            description = "특정 장소의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PlaceDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "여행 또는 장소를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDetailResponse> getPlace(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @Parameter(description = "장소 ID", required = true) @PathVariable Long placeId) {
        PlaceDetailResponse place = tripPlaceService.getPlace(tripId, placeId);
        return ResponseEntity.ok(place);
    }

    /*-----------------------------------------------------------------------------------------------*/

    @Operation(
            summary = "장소 등록",
            description = "multipart/form-data로 data(JSON) + images(file[])를 받습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = CreatePlaceMultipart.class)
                    )
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaceResponse> createPlace(
            @PathVariable Long tripId,
            @RequestPart("data") PlaceRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        Long placeId = tripPlaceService.createPlace(tripId, request, images);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PlaceResponse(placeId));
    }

    /** Swagger 문서용 multipart 스키마 */
    public static class CreatePlaceMultipart {
        @Schema(description = "장소 정보(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
        public PlaceRequest data;

        @Schema(description = "장소 이미지 파일 목록", type = "string", format = "binary",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        public List<MultipartFile> images;
    }

    /*-----------------------------------------------------------------------------------------------*/

    @Operation(
            summary = "장소 수정",
            description = "multipart/form-data로 data(JSON) + images(file[])를 받습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PlaceUpdateMultipart.class)
                    )
            )
    )
    @PatchMapping(value = "/{placeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaceResponse> updatePlace(
            @PathVariable Long tripId,
            @PathVariable Long placeId,
            @RequestPart("data") PlaceRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        // null/empty 필터링 (스웨거에서 빈 파일 들어오는 경우 방어)
        List<MultipartFile> imageList = null;
        if (images != null) {
            imageList = images.stream()
                    .filter(f -> f != null && !f.isEmpty())
                    .toList();
        }

        tripPlaceService.updatePlace(tripId, placeId, request, imageList);
        return ResponseEntity.ok(new PlaceResponse(placeId));
    }

    /** Swagger용 multipart wrapper */
    @Getter
    @Setter
    public static class PlaceUpdateMultipart {
        @Schema(description = "장소 정보(JSON)", requiredMode = Schema.RequiredMode.REQUIRED)
        private PlaceRequest data;

        @Schema(description = "장소 이미지 파일 목록", type = "string", format = "binary",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        private List<MultipartFile> images;
    }

    /*-----------------------------------------------------------------------------------------------*/

    @Operation(
            summary = "장소 삭제",
            description = "특정 장소를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "여행 또는 장소를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @Parameter(description = "여행 ID", required = true) @PathVariable Long tripId,
            @Parameter(description = "장소 ID", required = true) @PathVariable Long placeId) {
        tripPlaceService.deletePlace(tripId, placeId);
        return ResponseEntity.noContent().build();
    }

}
