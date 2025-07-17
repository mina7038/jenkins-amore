package com.apgroup.app.service;

import com.apgroup.app.entity.Member;
import com.apgroup.app.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private MemberRepository memberRepository; // 회원 저장용

    @Autowired
    private HttpSession httpSession; // 세션 저장용

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User user = new DefaultOAuth2UserService().loadUser(request);
        Map<String, Object> attributes = user.getAttributes();

        String registrationId = request.getClientRegistration().getRegistrationId();

        if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            String email = (String) kakaoAccount.get("email");

            if (email == null) {
                throw new OAuth2AuthenticationException("이메일 동의를 하지 않았습니다.");
            }

            String nickname = (String) ((Map<String, Object>) kakaoAccount.get("profile")).get("nickname");

            // 사용자 저장 또는 조회
            Member member = memberRepository.findByEmailIgnoreCase(email).orElse(null);
            if (member == null) {
                member = Member.builder()
                        .username(email)
                        .email(email)
                        .name(nickname)
                        .password("kakao_user")
                        .role("ROLE_USER")
                        .provider("kakao")
                        .build();
                memberRepository.save(member);
            }

            // 세션 저장
            httpSession.setAttribute("loginmember", member);
            System.out.println("✅ OAuth 세션 저장 완료: " + member.getUsername());
            System.out.println("✅ OAuth 권한: " + member.getRole());
            System.out.println("✅ OAuth 이메일: " + email);
            System.out.println("✅ OAuth 닉네임: " + nickname);

            Map<String, Object> customAttributes = new HashMap<>(attributes);
            customAttributes.put("email", email); // ✅ 명시적으로 추가

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                    customAttributes,
                    "email"
            );


        }

        return user;
    }


}

