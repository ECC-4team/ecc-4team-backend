package trip.diary.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 비밀번호를 안전하게 암호화해주는 객체
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API 개발 시 보통 끔
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/signup", "/users/login", "/users/logout", "/h2-console/**").permitAll() // 회원가입, H2 콘솔은 누구나 접근 가능
                        .anyRequest().authenticated() // 나머지는 로그인해야 접근 가능
                )
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable())); // H2 콘솔 깨짐 방지

        return http.build();
    }
}