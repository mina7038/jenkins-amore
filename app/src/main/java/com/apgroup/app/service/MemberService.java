package com.apgroup.app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apgroup.app.entity.Member;
import com.apgroup.app.repository.CartItemRepository;
import com.apgroup.app.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final CartItemRepository cartItemRepository;

	public long count() {
		return memberRepository.count(); // JPA 기본 제공 메서드
	}

	public Member findByEmail(String email) {
		return memberRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
	}

	//회원 가입
	public Member register(Member member) {
		member.setPassword(passwordEncoder.encode(member.getPassword()));
		member.setRole("ROLE_USER");
		return memberRepository.save(member);
	}
	//회원 정보 로딩
	public Member findByUsername(String username) {
		return memberRepository.findByUsername(username).orElse(null);
	}
	
	//회원 목록
	public Page<Member> findAll(Pageable pageable) {
		return memberRepository.findAll(pageable);
	}
	
	//회원 탈퇴
	@Transactional
	public void deleteById(Long id) {
	    cartItemRepository.deleteByUser_Id(id);  // 장바구니 먼저 삭제
	    memberRepository.deleteById(id);         // 회원 삭제
	}
	//회원 탈퇴2
	@Transactional
	public void deleteByUsername(String username) {
		memberRepository.deleteByUsername(username);
	}


	
	//회원 중복 아이디 체크
	public boolean existByUsername(String username) {
		return memberRepository.existsByUsername(username);
	}
	
	//회원 정보 수정 (edit)
	public Member edit(Member member) {
		Member origin = memberRepository.findById(member.getId())
				.orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다"));
		
		origin.setName(member.getName());
		origin.setTel(member.getTel());
		origin.setEmail(member.getEmail());
		if (member.getPassword() != null && !member.getPassword().isEmpty()) {
			origin.setPassword(passwordEncoder.encode(member.getPassword()));
		}
		return memberRepository.save(origin);
	}
}
