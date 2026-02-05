package trip.diary.entity;

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

    @Builder
    public Trip(User user, String title, String destination, LocalDate startDate, LocalDate endDate, int status, String imageUrl, String description) {
        this.user = user;
        this.title = title;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.imageUrl = imageUrl;
        this.description = description;
    }

    // 여행 정보 수정 메서드
    public void update(String title, String destination, LocalDate startDate, LocalDate endDate, String imageUrl, String description) {
        // 값이 들어온 경우에만 수정 (null이면 기존 값 유지)
        if (title != null) this.title = title;
        if (destination != null) this.destination = destination;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (description != null) this.description = description;

        // 날짜가 변경되었을 수 있으니 status(상태) 다시 계산
        if (this.endDate != null) {
            this.status = LocalDate.now().isAfter(this.endDate) ? 1 : 2;
        }
    }
}