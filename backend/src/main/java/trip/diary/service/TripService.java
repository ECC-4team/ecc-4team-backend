package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.*;
import trip.diary.entity.Trip;
import trip.diary.entity.User;
import trip.diary.global.exception.NotFoundException;
import trip.diary.repository.TripRepository;
import trip.diary.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    // 여행 생성
    public Long createTrip(TripCreateRequest request, String userId) {
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
                .status(status)
                .imageUrl(imageUrl)
                .description(request.getNote()) // note -> description 매핑
                .build();

        return tripRepository.save(trip).getId();
    }

    // 여행 목록 조회
    @Transactional(readOnly = true)
    public List<TripDto> getTrips(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        return tripRepository.findAllByUserOrderByStartDateDesc(user).stream()
                .map(TripDto::from)
                .collect(Collectors.toList());
    }

    // 여행 상세 조회 (GET /trips/{tripId})
    @Transactional(readOnly = true)
    public TripDetailDto getTripDetail(Long tripId, String userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 여행입니다."));

        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        return TripDetailDto.from(trip);
    }

    // 여행 수정 (PATCH)
    @Transactional // <--- ★ 데이터 변경 시 필수!
    public TripDetailDto updateTrip(Long tripId, TripUpdateRequest request, String userId) {
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
        return TripDetailDto.from(trip);
    }

    // 여행 삭제 (DELETE)
    @Transactional
    public void deleteTrip(Long tripId, String userId) {
        // 1. 여행 찾기
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 여행입니다."));

        // 2. 권한 확인 (작성자 검증)
        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 3. 삭제 수행
        tripRepository.delete(trip);
    }
}