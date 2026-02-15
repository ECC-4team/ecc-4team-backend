package trip.diary.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "trips")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id") // ERD: trip_id (PK)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // ERD: user_id (FK)
    private User user;

    @Column(length = 50)
    private String title; // ERD: title

    @Column(nullable = false, length = 100)
    private String destination; // ERD: destination

    @Column(name = "is_domestic")
    private Boolean isDomestic; // true: 국내, false: 해외

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate; // ERD: start_date

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate; // ERD: end_date

    @Column(nullable = false)
    private int status; // ERD: status (1: 다녀온 여행, 2: 새로운 여행)

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl; // ERD: image_url (기본 이미지 처리가 필요함)

    @Column(columnDefinition = "TEXT")
    private String description; // ERD: description (요청의 note 매핑)

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // ERD: created_at

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // ERD: updated_at

    // 1. 여행 삭제 시 -> 연관된 장소(Place)들도 함께 삭제
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Place> places = new ArrayList<>();

    // 2. 여행 삭제 시 -> 연관된 일정(TripDay)들도 함께 삭제
    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripDay> tripDays = new ArrayList<>();

    @Builder
    public Trip(User user, String title, String destination, Boolean isDomestic, LocalDate startDate, LocalDate endDate, int status, String imageUrl, String description) {
        this.user = user;
        this.title = title;
        this.destination = destination;
        this.isDomestic = isDomestic;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // 여행 정보 수정 메서드
    public void update(String title, String destination, Boolean isDomestic, String imageUrl, String description) {
        // 값이 들어온 경우에만 수정 (null이면 기존 값 유지)
        if (title != null) this.title = title;
        if (destination != null) this.destination = destination;
        if (isDomestic != null) this.isDomestic = isDomestic;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (description != null) this.description = description;

        /* 날짜 수정시 상태 계산 로직
        if (this.endDate != null) {
            this.status = LocalDate.now().isAfter(this.endDate) ? 1 : 2;
        } */
    }
}