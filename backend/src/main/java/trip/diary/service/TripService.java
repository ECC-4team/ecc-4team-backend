package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.TripCreateRequest;
import trip.diary.dto.TripCreateResponse;
import trip.diary.entity.Trip;
import trip.diary.entity.User;
import trip.diary.repository.TripRepository;
import trip.diary.repository.UserRepository;

import java.time.LocalDate;

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

        // 3. 기본 이미지 설정
        String defaultImageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e";

        // 4. Trip 엔티티 생성
        Trip trip = Trip.builder()
                .user(user)
                .title(request.getTitle())
                .destination(request.getDestination())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status) // 계산된 상태값
                .imageUrl(defaultImageUrl) // 기본값
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
}