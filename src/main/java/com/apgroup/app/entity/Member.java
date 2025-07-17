package com.apgroup.app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String username;
	
	@Column(nullable = false)
	private String password;

    @Column(nullable = false)
    private String name;

	@Column(nullable = false, unique = true)
	private String email;
	private String provider;
	private String tel;
	
	private String postcode;
	private String addr1;
	private String addr2;
	
	private String role;	//ROLE_USER, ROLE_ADMIN
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
	@Builder.Default
	private List<CartItem> cartItems = new ArrayList<>();
	
	@Builder.Default
	private LocalDateTime regdate = LocalDateTime.now();
}