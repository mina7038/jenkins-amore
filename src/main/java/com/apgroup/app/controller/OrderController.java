package com.apgroup.app.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.apgroup.app.entity.Order;
import com.apgroup.app.security.CustomUserDetails;
import com.apgroup.app.service.OrderService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	// ✅ 결제 성공 페이지
	@GetMapping("/complete")
	public String orderComplete(@RequestParam("orderId") String orderId, Model model) {
		Order order = orderService.getOrderByOrderId(orderId);
		model.addAttribute("order", order);
		return "order/complete";
	}

	// ✅ 결제 실패 페이지
	@GetMapping("/fail")
	public String orderFail(@RequestParam("orderId") String orderId, Model model) {
		Order order = orderService.getOrderByOrderId(orderId);
		model.addAttribute("order", order);
		return "order/fail";
	}

	// ✅ 결제 취소 페이지
	@GetMapping("/cancel")
	public String orderCancel(@RequestParam("orderId") String orderId, Model model) {
		Order order = orderService.getOrderByOrderId(orderId);
		model.addAttribute("order", order);
		return "order/cancel";
	}

	@GetMapping("/mypage")
	public String viewOrderList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
		if (userDetails == null) {
			return "redirect:/login"; // 로그인하지 않은 경우
		}

		Long userId = userDetails.getMember().getId(); // ← 여기서 ID 추출
		List<Order> orders = orderService.findOrdersByUserId(userId);
		model.addAttribute("orders", orders);
		return "mypage/orders"; // 주문 목록 템플릿
	}

	@GetMapping("/detail/{id}")
	public String viewOrderDetail(@AuthenticationPrincipal CustomUserDetails userDetails,
								  @PathVariable("id") Long id,
								  Model model) {
		if (userDetails == null) {
			return "redirect:/login";
		}

		Order order = orderService.getOrderById(id);

		// 주문한 사용자인지 검증 (보안)
		if (!order.getUser().getId().equals(userDetails.getMember().getId())) {
			return "redirect:/orders/mypage"; // 본인 주문이 아니면 접근 차단
		}

		model.addAttribute("order", order);
		return "mypage/order-detail"; // 템플릿 경로
	}
}
