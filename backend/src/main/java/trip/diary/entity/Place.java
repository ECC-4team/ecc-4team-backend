package trip.diary.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "places")
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;              // placeId

    @Column(nullable = false)
    private Long tripId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    private String coverImageUrl;

    /**
     * 상세 조회용 이미지 URL 목록
     * - JSON 컬럼 or @ElementCollection 둘 중 하나
     * - 여기서는 가장 단순한 ElementCollection 사용
     */
    @ElementCollection
    @CollectionTable(
            name = "place_images",
            joinColumns = @JoinColumn(name = "place_id")
    )
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /* ===== 생명주기 ===== */

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}