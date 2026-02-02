package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.UserLoginRequest;
import trip.diary.dto.UserLoginResponse;
import trip.diary.dto.UserSignupRequest;
import trip.diary.entity.User;
import trip.diary.global.jwt.JwtTokenProvider; // [추가]
import trip.diary.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public User signup(UserSignupRequest request) {
        // 1. 아이디 중복 검사
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. User 엔티티 생성
        User user = User.builder()
                .userId(request.getUserId())
                .password(encodedPassword)
                .build();

        // 4. DB 저장
        return userRepository.save(user);
    }

    public UserLoginResponse login(UserLoginRequest request) {
        // 1. 유저 확인
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getUserId());

        // 4. 응답 DTO 생성 (요청하신 포맷에 맞춤)
        UserLoginResponse.LoginData loginData = UserLoginResponse.LoginData.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .accessToken(accessToken)
                .build();

        return new UserLoginResponse(200, "로그인에 성공했습니다.", loginData);
    }
}