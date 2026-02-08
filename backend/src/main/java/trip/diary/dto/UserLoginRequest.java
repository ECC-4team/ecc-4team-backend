package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class UserLoginRequest {

    @Schema(description = "사용자 아이디", example = "example123")
    @NotBlank(message = "아이디를 입력해주세요.")
    private String userId;

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}