package com.apgroup.app;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.apgroup.app.entity.Member;
import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.security.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomLoginSuccessHandler(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 기존 세션 무효화
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        Object principal = authentication.getPrincipal();

        if (principal instanceof OAuth2User oAuth2User) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) oAuth2User.getAttribute("kakao_account");
            String email = (String) kakaoAccount.get("email");
            String nickname = (String) ((Map<String, Object>) kakaoAccount.get("profile")).get("nickname");

            if (email == null) {
                throw new OAuth2AuthenticationException("이메일 정보가 없습니다.");
            }

            Member member = memberRepository.findByEmailIgnoreCase(email).orElse(null);
            if (member == null) {
                member = Member.builder()
                        .username(email)
                        .email(email)
                        .name(nickname)
                        .password(passwordEncoder.encode("kakao1234"))
                        .role("ROLE_USER")
                        .provider("kakao")
                        .build();
                memberRepository.save(member);
            }

            CustomUserDetails userDetails = new CustomUserDetails(member);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // ✅ SecurityContext 설정
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);

            // ✅ 세션에 SecurityContext 저장
            newSession.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );



            newSession.setAttribute("loginmember", member);
            System.out.println("✅ OAuth 로그인 성공: " + member.getUsername());

            response.sendRedirect("/");
        }

        else if (principal instanceof CustomUserDetails userDetails) {
            Member member = userDetails.getMember();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // SecurityContext 수동 설정
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);

            // 세션에도 SecurityContext 저장
            newSession.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
            );

            newSession.setAttribute("loginmember", member);
            System.out.println("[✅ 일반 로그인 성공] 사용자: " + member.getUsername() + ", provider: " + member.getProvider());

            // 직접 리디렉션
            response.sendRedirect("/");
        }

    }
}
