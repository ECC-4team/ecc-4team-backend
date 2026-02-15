package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.*;
import trip.diary.entity.Trip;
import trip.diary.entity.TripDay;
import trip.diary.entity.User;
import trip.diary.global.exception.NotFoundException;
import trip.diary.repository.TripDayRepository;
import trip.diary.repository.TripRepository;
import trip.diary.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import trip.diary.global.image.ImageStorageService;

@Service
@Transactional
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final UserRepository userRepository;
    private final TripDayRepository tripDayRepository;
    private final ImageStorageService imageStorageService;

    private static final String DEFAULT_IMAGE_URL = "https://i.imgur.com/5eDmhnp.jpeg";

    // 여행 생성
    public Long createTrip(TripCreateRequest request, MultipartFile image, String userId) {
        // 현재 로그인한 유저 찾기
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        // 날짜 유효성 검사 (종료일 < 시작일)
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("여행 종료일은 시작일보다 빠를 수 없습니다.");
        }

        // 이미지 처리 로직
        String imageUrl = DEFAULT_IMAGE_URL;
        if (image != null && !image.isEmpty()) {
            // 파일을 서버에 저장하고 경로를 받아옴
            imageUrl = imageStorageService.upload(image);
        }

        // 여행 상태(Status) 판별
        // 오늘 날짜가 종료일보다 지났으면 1(다녀온 여행), 아니면 2(새로운/진행중 여행)
        int status = LocalDate.now().isAfter(request.getEndDate()) ? 1 : 2;

        // Trip 엔티티 생성
        Trip trip = Trip.builder()
                .user(user)
                .title(request.getTitle())
                .destination(request.getDestination())
                .isDomestic(request.getIsDomestic())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(status)
                .imageUrl(imageUrl)
                .description(request.getDescription()) // note -> description 매핑
                .build();

        // Trip 먼저 저장해서 trip_id 확보
        Trip savedTrip = tripRepository.save(trip);

        //여행 기간만큼 TripDay 자동 생성
        createTripDays(savedTrip);

        return savedTrip.getId();
    }

    private void createTripDays(Trip trip) {

        LocalDate date = trip.getStartDate();
        LocalDate end = trip.getEndDate();

        int dayIndex = 1;
        List<TripDay> days = new ArrayList<>();

        while (!date.isAfter(end)) {
            TripDay day = TripDay.create(trip, date, dayIndex);
            days.add(day);

            date = date.plusDays(1);//날짜 하루 증가
            dayIndex++;//인덱스 하나 증가
        }

        tripDayRepository.saveAll(days);

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

    // 여행 상세 조회
    @Transactional(readOnly = true)
    public TripDetailDto getTripDetail(Long tripId, String userId) {
        // 유저 확인
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        // 여행 조회 (없으면 에러)
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 여행입니다."));

        // 권한 확인
        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("조회 권한이 없습니다.");
        }

        return TripDetailDto.from(trip);
    }

    // 여행 수정
    @Transactional
    public TripDetailDto updateTrip(Long tripId, TripUpdateRequest request, MultipartFile image, String userId) {
        // 여행 찾기
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 여행입니다."));

        // 권한 확인 (내 여행인지)
        if (!trip.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // 이미지 처리 로직
        String imageUrlToUse = trip.getImageUrl(); // 기본적으로 기존 이미지 유지

        if (image != null && !image.isEmpty()) {
            // 새 파일이 들어왔으면 Cloudinary 업로드 후 URL 교체
            imageUrlToUse = imageStorageService.upload(image);
        }

        /* 날짜 유효성 검사 로직
        LocalDate startDateToCheck = (request.getStartDate() != null) ? request.getStartDate() : trip.getStartDate();
        LocalDate endDateToCheck = (request.getEndDate() != null) ? request.getEndDate() : trip.getEndDate();

        if (endDateToCheck.isBefore(startDateToCheck)) {
            throw new IllegalArgumentException("여행 종료일은 시작일보다 빠를 수 없습니다.");
        } */

        // 내용 수정 (Entity의 update 메서드 호출)
        trip.update(
                request.getTitle(),
                request.getDestination(),
                request.getIsDomestic(),
                imageUrlToUse,
                request.getDescription()
        );

        // 수정된 결과 반환
        return TripDetailDto.from(trip);
    }

    // 여행 삭제
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