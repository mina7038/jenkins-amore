package com.apgroup.app.controller;

import java.security.Principal;
import java.util.List;

import com.apgroup.app.entity.Order;
import com.apgroup.app.entity.OrderItem;
import com.apgroup.app.entity.OrderStatusDto;
import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.repository.OrderRepository;
import com.apgroup.app.repository.ReviewRepository;
import com.apgroup.app.security.CustomUserDetails;
import com.apgroup.app.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.apgroup.app.entity.Member;
import com.apgroup.app.service.MemberService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/member")
public class MemberController {

	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final OrderService orderService;
	private final ReviewRepository reviewRepository;

	@GetMapping("/login")
	public String login() {
		return "member/login";
	}

	@GetMapping("/check-username")
	@ResponseBody
	public boolean checkUsername(@RequestParam("username") String username) {
		return !memberService.existByUsername(username); // true면 사용 가능
	}

	// ✅ 1단계: 약관 동의 화면
	@GetMapping("/join/step1")
	public String step1Form() {
		return "member/step1"; // => templates/member/step1.html
	}

	// ✅ 1단계: 약관 동의 처리
	@PostMapping("/join/step1")
	public String handleStep1(@RequestParam boolean agree_privacy,
							  @RequestParam boolean agree_terms,
							  @RequestParam(required = false) boolean agree_marketing,
							  HttpSession session) {
		if (!agree_privacy || !agree_terms) {
			return "redirect:/member/join/step1?error";
		}
		session.setAttribute("agree_privacy", agree_privacy);
		session.setAttribute("agree_terms", agree_terms);
		session.setAttribute("agree_marketing", agree_marketing);
		return "redirect:/member/join/step2";
	}

	// ✅ 2단계: 회원정보 입력 화면
	@GetMapping("/join/step2")
	public String step2Form(Model model, HttpSession session) {
		Boolean agreePrivacy = (Boolean) session.getAttribute("agree_privacy");
		Boolean agreeTerms = (Boolean) session.getAttribute("agree_terms");

		// 세션 없으면 다시 step1으로 보냄
		if (agreePrivacy == null || agreeTerms == null || !agreePrivacy || !agreeTerms) {
			return "redirect:/member/join/step1";
		}

		model.addAttribute("member", new Member());
		return "member/step2"; // => templates/member/step2.html
	}

	// ✅ 최종 회원가입 처리 (step2 POST)
	@PostMapping("/join")
	public String join(@ModelAttribute Member member) {
		memberService.register(member);
		return "redirect:/member/login";
	}

	@GetMapping("/mypage")
	public String mypage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		if (userDetails == null) {
			return "redirect:/member/login";
		}

		Member member = userDetails.getMember();
		model.addAttribute("member", member);

		// 🔧 전화번호 3칸 분리
		if (member.getTel() != null) {
			String[] parts = member.getTel().split("-");
			if (parts.length == 3) {
				model.addAttribute("tel1", parts[0]);
				model.addAttribute("tel2", parts[1]);
				model.addAttribute("tel3", parts[2]);
			}
		}

		// ✅ 주문 처리 현황 정보
		OrderStatusDto orderStatus = orderService.getOrderSummary(member.getId());
		model.addAttribute("orderStatus", orderStatus);

		// ✅ 최근 주문 목록
		List<Order> recentOrders = orderService.getRecentOrders(member.getId(), 5);

		// 🔍 각 아이템별 리뷰 작성 여부 추가
		for (Order order : recentOrders) {
			for (OrderItem item : order.getItems()) {
				boolean hasReview = reviewRepository.existsByMemberAndGoodsAndOrder(member, item.getGoods(), order);
				item.setHasReview(hasReview); // ✅ setter 필요
			}
		}

		model.addAttribute("recentOrders", recentOrders);
		return "member/mypage";
	}


	@GetMapping("/delete")
	public String delete(Principal principal) {
		String username = principal.getName();
		memberService.deleteByUsername(username);
		return "redirect:/member/login";
	}

	@GetMapping("/delete/{id}")
	public String deleteMember(@PathVariable Long id) {
		memberService.deleteById(id);
		return "redirect:/admin/members";
	}

	@GetMapping("/logout")
	public String logout() {
		return "redirect:/";
	}

	@GetMapping("/edit")
	public String editLoad(Authentication auth, Model model) {
		String username = auth.getName();
		Member member = memberService.findByUsername(username);
		model.addAttribute("member", member);
		if (member.getTel() != null) {
			String[] parts = member.getTel().split("-");
			if (parts.length == 3) {
				model.addAttribute("tel1", parts[0]);
				model.addAttribute("tel2", parts[1]);
				model.addAttribute("tel3", parts[2]);
			}
		}
		return "mypage/user-edit";
	}

	@PostMapping("/edit")
	public String edit(HttpServletRequest request) {
		String tel = request.getParameter("tel1") + "-" +
				request.getParameter("tel2") + "-" +
				request.getParameter("tel3");

		Member member = memberService.findByUsername(request.getUserPrincipal().getName());
		member.setTel(tel);
		memberService.edit(member);
		return "redirect:/member/edit";
	}
}
