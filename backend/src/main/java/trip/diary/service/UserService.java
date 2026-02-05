package trip.diary.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trip.diary.dto.UserLoginDto;
import trip.diary.dto.UserLoginRequest;
import trip.diary.dto.UserSignupRequest;
import trip.diary.entity.User;
import trip.diary.global.exception.NotFoundException;
import trip.diary.global.jwt.JwtTokenProvider;
import trip.diary.repository.UserRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    public User signup(UserSignupRequest request) {
        // 1. 아이디 중복 검사
        if (userRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
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

    // 로그인
    public UserLoginDto login(UserLoginRequest request) {
        // 1. 유저 확인
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.createToken(user.getUserId());

        return UserLoginDto.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .accessToken(accessToken)
                .build();
    }

    // 로그아웃
    public void logout(String authHeader) {
        // 1. 헤더가 없거나 형식이 틀린 경우
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 정보가 유효하지 않습니다.");
        }

        // 2. 토큰 값만 추출 ("Bearer " 제거)
        String token = authHeader.substring(7);

        // 3. 토큰 자체가 유효한지 검사 (위변조, 만료 등)
        if (!jwtTokenProvider.validateToken(token)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
    }
}