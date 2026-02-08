package trip.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "회원가입 요청 DTO")
public class UserSignupRequest {

    @Schema(description = "사용자 아이디 (영문 소문자/숫자 포함 5~20자)", example = "example123")
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    @Size(min = 5, max = 20, message = "아이디는 5자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-z0-9]{5,20}$", message = "아이디는 영문 소문자와 숫자만 사용 가능합니다.") // 공백X, 특수문자X
    private String userId;

    @Schema(description = "비밀번호 (영문/숫자/특수문자 포함 8~20자)", example = "password123!")
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*?_]).{8,20}$", message = "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다.")
    private String password;
}