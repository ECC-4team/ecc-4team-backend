package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "로그인 성공 응답 DTO")
public class UserLoginDto {

    @Schema(description = "유저 DB PK", example = "1")
    private Long id;

    @Schema(description = "유저 아이디", example = "example123")
    private String userId;

    @Schema(description = "JWT 액세스 토큰 (이 값을 헤더에 넣어야 함)", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ...")
    private String accessToken;
}