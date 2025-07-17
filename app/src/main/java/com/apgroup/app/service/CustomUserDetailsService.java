package com.apgroup.app.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.apgroup.app.entity.Member;
import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("사용자 없음: " + username));

        if (member.getRole() == null || member.getRole().isBlank()) {
            throw new IllegalArgumentException("회원의 권한 정보가 없습니다.");
        }

        return new CustomUserDetails(member); // ✅ 커스텀 객체 반환!
    }
}
