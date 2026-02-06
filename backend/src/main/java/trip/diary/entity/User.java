package trip.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users") // ERD 테이블명: users
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // ERD: id (INT)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true, length = 30)
    private String userId; // ERD: user_id

    @Column(name = "password_hash", nullable = false, length = 255) // ERD: password_hash
    private String password;

    @CreatedDate
    @Column(name = "created_at", updatable = false) // ERD: created_at
    private LocalDateTime createdAt;

    @Builder
    public User(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }
}