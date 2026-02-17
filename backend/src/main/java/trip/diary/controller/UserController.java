package trip.diary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trip.diary.dto.CommonResponse;
import trip.diary.dto.UserLoginDto;
import trip.diary.dto.UserLoginRequest;
import trip.diary.dto.UserSignupRequest;
import trip.diary.entity.User;
import trip.diary.service.UserService;
import java.util.Map;

@Tag(name = "회원 관리 API", description = "회원가입, 로그인, 로그아웃 기능")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 유효성 검사 실패",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "1. 입력값 누락", value = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "message": "{아이디/비밀번호}는 필수 입력 값입니다."
                                            }
                                            """),
                                    @ExampleObject(name = "2. 아이디 조건 미충족", value = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "message": "아이디는 영문 소문자와 숫자만 사용 가능합니다."
                                            }
                                            """),
                                    @ExampleObject(name = "3. 비밀번호 조건 미충족", value = """
                                            {
                                                "success": false,
                                                "data": null,
                                                "message": "비밀번호는 8자 이상, 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다."
                                            }
                                            """)
                            })),

            // ▼▼▼ 409 에러: 비즈니스 로직 충돌 (아이디 중복) ▼▼▼
            @ApiResponse(responseCode = "409", description = "이미 존재하는 데이터",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "아이디 중복", value = """
                                    {
                                        "success": false,
                                        "data": null,
                                        "message": "이미 존재하는 아이디입니다."
                                    }
                                    """)
                    ))
    })

    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Map<String, String>>> signup(
            @RequestBody @Valid UserSignupRequest request) {

        User savedUser = userService.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(Map.of(
                        "userId", savedUser.getUserId(),
                        "message", "회원가입이 완료되었습니다."
                )));
    }

    // 로그인
    @Operation(summary = "로그인", description = "ID와 비밀번호를 입력하여 인증 토큰(JWT)을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = UserLoginDto.class))),

            @ApiResponse(responseCode = "400", description = "로그인 실패",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "1. 아이디/비밀번호 미입력", summary = "필수 입력값 누락",
                                            value = """
                                                    {
                                                      "message": "아이디를 입력해주세요."
                                                    }
                                                    """),
                                    @ExampleObject(name = "2. 로그인 정보 불일치", summary = "가입되지 않은 아이디거나 비밀번호 틀림",
                                            value = """
                                                    {
                                                      "message": "아이디 또는 비밀번호가 일치하지 않습니다."
                                                    }
                                                    """)
                            }))
    })
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<UserLoginDto>> login(
            @RequestBody @Valid UserLoginRequest request) {

        UserLoginDto loginDto = userService.login(request);
        return ResponseEntity.ok(CommonResponse.success(loginDto));
    }

    // 로그아웃
    @Operation(summary = "로그아웃", description = "현재 로그인된 계정에서 로그아웃. (토큰은 유효시간으로 처리)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),

            // ▼▼▼ 401 에러: 인증 실패 (헤더 오류 vs 토큰 오류) ▼▼▼
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "1. 헤더 형식 오류",
                                            summary = "Authorization 헤더 누락 또는 Bearer 아님",
                                            value = """
                                                    {
                                                      "message": "인증 정보가 유효하지 않습니다."
                                                    }
                                                    """),
                                    @ExampleObject(name = "2. 토큰 유효성 오류",
                                            summary = "토큰 만료, 위변조, 손상됨",
                                            value = """
                                                    {
                                                      "message": "유효하지 않은 토큰입니다."
                                                    }
                                                    """)
                            }))
    })
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<String>> logout(
            @Parameter(hidden = true) @RequestHeader(value = "Authorization", required = false) String authHeader) {

        userService.logout(authHeader);
        return ResponseEntity.ok(CommonResponse.success("로그아웃 되었습니다."));
    }

    /* 내 정보 확인 (테스트용)
    @Operation(summary = "내 정보 확인 (토큰 테스트용)", description = "유효한 토큰을 보내면 성공 응답을 줍니다.")
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<String>> myInfo() {
        return ResponseEntity.ok(CommonResponse.success("인증된 유저입니다!"));
    }
    */
}