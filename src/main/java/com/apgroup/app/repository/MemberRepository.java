package com.apgroup.app.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apgroup.app.entity.Member;


public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findByUsername(String username);
	void deleteByUsername(String username);
	boolean existsByUsername(String username);
	Page<Member> findAll(Pageable pageable);
	Optional<Member> findByEmailIgnoreCase(String email);
}