package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.*;
import trip.diary.entity.Trip;
import trip.diary.entity.User;
import trip.diary.repository.TripRepository;
import trip.diary.repository.UserRepository;

import java.util.Map;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    public TripCreateResponse createTrip(TripCreateRequest request, String userId) {
        // 1. 현재 로그인한 유저 찾기
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 여행 상태(Status) 판별
        // 오늘 날짜가 종료일보다 지났으면 1(다녀온 여행), 아니면 2(새로운/진행중 여행)
        int status = LocalDate.now().isAfter(request.getEndDate()) ? 1 : 2;

        // 3. 이미지 설정
        String imageUrl;
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            // 사용자가 보낸 이미지
            imageUrl = request.getImageUrl();
        } else {
            // 없으면 기본 이미지 사용
            imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e";
        }

        // 4. Trip 엔티티 생성
        Trip trip = Trip.builder()
                .user(user)
                .title(request.getTitle())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status) // 계산된 상태값
                .imageUrl(imageUrl)
                .description(request.getNote()) // note -> description 매핑
                .build();

        // 5. DB 저장
        Trip savedTrip = tripRepository.save(trip);

        // 6. 응답 생성
        return new TripCreateResponse(
                201,
                "여행이 생성되었습니다.",
                new TripCreateResponse.TripIdData(savedTrip.getId())
        );
    }

    // 여행 목록 조회
    @Transactional(readOnly = true)
    public TripListResponse getTrips(String userId) {
        // 1. 유저 확인
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. DB에서 여행 목록 조회 (최신순)
        List<Trip> trips = tripRepository.findAllByUserOrderByStartDateDesc(user);

        // 3. Entity -> DTO 변환
        List<TripDto> tripDtos = trips.stream()
                .map(TripDto::from)
                .toList();

        // 4. 최종 응답 반환
        return new TripListResponse(200, "여행 목록 조회 성공", tripDtos);
    }

    // 여행 상세 조회 (GET /trips/{tripId})
    @Transactional(readOnly = true)
    public Map<String, Object> getTripDetail(Long tripId, String userId) {
        // 1. 여행 찾기
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));

        // 2. 권한 확인 (작성자 본인만 조회 가능)
        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        // 3. DTO 변환
        TripDetailDto data = TripDetailDto.from(trip);

        // 4. 응답 구조 맞추기 (code, message, data)
        return Map.of(
                "code", 200,
                "message", "여행 상세 정보를 조회했습니다.",
                "data", data
        );
    }

    // 여행 수정 (PATCH)
    @Transactional // <--- ★ 데이터 변경 시 필수!
    public Map<String, Object> updateTrip(Long tripId, TripUpdateRequest request, String userId) {
        // 1. 여행 찾기
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));

        // 2. 권한 확인 (내 여행인지)
        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // 3. 내용 수정 (Entity의 update 메서드 호출)
        trip.update(
                request.getTitle(),
                request.getDestination(),
                request.getStartDate(),
                request.getEndDate(),
                request.getImageUrl(),
                request.getNote()
        );

        // 4. 수정된 결과 반환 (프론트엔드 반영용)
        TripDetailDto data = TripDetailDto.from(trip);

        return Map.of(
                "code", 200,
                "message", "여행 정보가 수정되었습니다.",
                "data", data
        );
    }

    // 여행 삭제 (DELETE)
    @Transactional
    public Map<String, Object> deleteTrip(Long tripId, String userId) {
        // 1. 여행 찾기
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 여행입니다."));

        // 2. 권한 확인 (작성자 검증)
        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 3. 삭제 수행
        tripRepository.delete(trip);

        // 4. 응답 생성 (data: null 처리를 위해 HashMap 사용)
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("code", 200);
        response.put("message", "여행이 삭제되었습니다.");
        response.put("data", null);

        return response;
    }
}