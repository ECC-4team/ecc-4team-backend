package trip.diary.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@Entity
@Getter
@Setter
@Table(name = "timeline_items")
public class TimelineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "day_id", nullable = false)
    private TripDay day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id",nullable = false)
    private Place place;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static TimelineItem create(TripDay day, LocalTime startTime, LocalTime endTime, Place place) {

        TimelineItem item = new TimelineItem();
        item.day = day;
        item.startTime = startTime;
        item.endTime = endTime;
        item.place = place; // null 가능(장소 선택 안 한 일정)

        return item;
    }

}
