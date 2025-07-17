package com.apgroup.app.controller;

import java.util.List;

import com.apgroup.app.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.apgroup.app.dto.CartItemSummaryDto;
import com.apgroup.app.dto.KakaoPayRequestDto;
import com.apgroup.app.dto.KakaoPayResponseDto;
import com.apgroup.app.entity.Goods;
import com.apgroup.app.entity.Member;
import com.apgroup.app.entity.Order;
import com.apgroup.app.repository.GoodsRepository;
import com.apgroup.app.repository.MemberRepository;
import com.apgroup.app.service.KakaoPayService;
import com.apgroup.app.service.OrderService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class KaKaoPayController {

    private final KakaoPayService kakaoPayService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final GoodsRepository goodsRepository;

    @GetMapping("/shipping")
    public String shippingForm(@RequestParam("type") String type,
                               @RequestParam(required = false, value="userId") Long userId,
                               @RequestParam(required = false, value="goodsId") Long goodsId,
                               @RequestParam(required = false, value="quantity") Integer quantity,
                               Model model, HttpSession session) {

        model.addAttribute("type", type);
        model.addAttribute("userId", userId);
        model.addAttribute("goodsId", goodsId);
        model.addAttribute("quantity", quantity);

        if ("cart".equals(type)) {
            @SuppressWarnings("unchecked")
            List<Long> cartItemIds = (List<Long>) session.getAttribute("cartItemIds");
            model.addAttribute("cartItemIds", cartItemIds);
        }

        if (userId != null) {
            memberRepository.findById(userId).ifPresent(member -> {
                model.addAttribute("defaultName", member.getName());
                model.addAttribute("defaultPhone", member.getTel());
                model.addAttribute("defaultPostcode", member.getPostcode());
                model.addAttribute("defaultAddr1", member.getAddr1());
                model.addAttribute("defaultAddr2", member.getAddr2());
            });
        }

        return "payment/shipping";
    }

    @PostMapping("/shipping")
    public String handleShippingForm(@RequestParam("name") String name,
                                     @RequestParam("phone") String phone,
                                     @RequestParam("postcode") String postcode,
                                     @RequestParam("addr1") String addr1,
                                     @RequestParam("addr2") String addr2,
                                     @RequestParam("type") String type,
                                     @RequestParam(required = false, value="userId") Long userId,
                                     @RequestParam(required = false, value="goodsId") Long goodsId,
                                     @RequestParam(required = false, value="quantity") Integer quantity,
                                     @RequestParam(required = false, value="cartItemIds") List<Long> cartItemIds,
                                     HttpSession session) {

        session.setAttribute("shippingName", name);
        session.setAttribute("shippingPhone", phone);
        session.setAttribute("shippingAddress", postcode + " " + addr1 + " " + addr2);
        session.setAttribute("type", type);
        session.setAttribute("userId", userId);
        session.setAttribute("goodsId", goodsId);
        session.setAttribute("quantity", quantity);

        if ("cart".equals(type) && cartItemIds != null) {
            session.setAttribute("cartItemIds", cartItemIds);
        }

        return "redirect:/payment/ready";
    }

    @GetMapping("/shipping/selected")
    public String shippingFromSelected(@RequestParam("userId") Long userId,
                                       @RequestParam("cartIds") List<Long> cartIds,
                                       HttpSession session) {
        session.setAttribute("type", "cart");
        session.setAttribute("userId", userId);
        session.setAttribute("cartItemIds", cartIds);
        return "redirect:/payment/shipping?type=cart&userId=" + userId;
    }

    @GetMapping("/ready")
    public String paymentReady(HttpSession session, Model model) {
        String name = (String) session.getAttribute("shippingName");
        String phone = (String) session.getAttribute("shippingPhone");
        String address = (String) session.getAttribute("shippingAddress");
        String type = (String) session.getAttribute("type");
        Long userId = (Long) session.getAttribute("userId");

        model.addAttribute("receiver", name);
        model.addAttribute("phone", phone);
        model.addAttribute("address", address);
        model.addAttribute("type", type);
        model.addAttribute("userId", userId);
        model.addAttribute("orderId", "ORDER_" + System.currentTimeMillis());

        switch (type) {
            case "product" -> {
                Long goodsId = (Long) session.getAttribute("goodsId");
                Integer quantity = (Integer) session.getAttribute("quantity");
                Goods goods = goodsRepository.findById(goodsId).orElse(null);
                if (goods == null) return "redirect:/";

                int unitPrice = goods.getPrice2();
                int totalPrice = unitPrice * quantity;
                int shippingFee = totalPrice >= 30000 ? 0 : 3000;
                int finalAmount = totalPrice + shippingFee;

                model.addAttribute("items", List.of(
                        new CartItemSummaryDto(goods.getGname(), quantity, unitPrice, totalPrice, goods.getImg1(), goods.getNo())));
                model.addAttribute("amount", finalAmount);
                model.addAttribute("shippingFee", shippingFee);
                model.addAttribute("goodsId", goodsId);
                model.addAttribute("quantity", quantity);
            }
            case "cart" -> {
                @SuppressWarnings("unchecked")
                List<Long> cartItemIds = (List<Long>) session.getAttribute("cartItemIds");
                if (cartItemIds == null || cartItemIds.isEmpty()) {
                    throw new IllegalArgumentException("cartItemIds가 비어 있음");
                }
                int totalAmount = orderService.calculateTotalAmountFromCart(userId, cartItemIds);
                int shippingFee = totalAmount >= 30000 ? 0 : 3000;
                int finalAmount = totalAmount + shippingFee;
                List<CartItemSummaryDto> items = orderService.getCartItemSummaries(userId, cartItemIds);

                model.addAttribute("items", items);
                model.addAttribute("amount", finalAmount);
                model.addAttribute("shippingFee", shippingFee);
                model.addAttribute("cartItemIds", cartItemIds); // JS 배열로 직접 렌더링할 것
            }
            case "cartAll" -> {
                int totalAmount = orderService.calculateTotalAmountFromWholeCart(userId);
                int shippingFee = totalAmount >= 30000 ? 0 : 3000;
                int finalAmount = totalAmount + shippingFee;
                String orderName = orderService.generateOrderNameFromWholeCart(userId);
                int quantity = orderService.countItemsInWholeCart(userId);
                List<CartItemSummaryDto> items = orderService.getCartItemSummariesFromWholeCart(userId); // ✅ 추가

                model.addAttribute("orderName", orderName);
                model.addAttribute("quantity", quantity);
                model.addAttribute("amount", finalAmount);
                model.addAttribute("shippingFee", shippingFee);
                model.addAttribute("items", items); // ✅ 추가
            }
        }

        return "payment/confirm";
    }

    @PostMapping("/kakaopay/request")
    public ResponseEntity<?> requestKakaoPay(@RequestBody KakaoPayRequestDto dto) {
        Order order;
        switch (dto.getType()) {
            case "product" -> order = orderService.prepareOrderFromProduct(
                    dto.getUserId(), dto.getGoodsId(), dto.getQuantity(), dto.getOrderId());
            case "cart" -> order = orderService.prepareOrderFromCart(
                    dto.getUserId(), dto.getCartItemIds(), dto.getOrderId());
            case "cartAll" -> order = orderService.prepareOrderFromWholeCart(
                    dto.getUserId(), dto.getOrderId());
            default -> {
                return ResponseEntity.badRequest().body("결제 타입 오류");
            }
        }

        KakaoPayResponseDto kakaoRes = kakaoPayService.requestPayment(order);
        return ResponseEntity.ok(kakaoRes.getRedirectUrl());
    }

    @GetMapping("/success")
    public String kakaoPaySuccess(@RequestParam("pg_token") String pg_token,
                                  @RequestParam("orderId") String orderId,
                                  Model model) {
        // 1. 결제 승인
        kakaoPayService.approvePayment(pg_token, orderId);

        // 2. 주문 확정 및 저장 (Order + OrderItems 저장)
        orderService.confirmOrder(orderId);

        // 3. 저장된 주문을 조회해서 템플릿에 전달
        Order order = orderRepository.findByOrderIdWithItems(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        model.addAttribute("order", order);
        return "payment/success";
    }

    @GetMapping("/fail")
    public String kakaoPayFail(@RequestParam("orderId") String orderId, Model model) {
        orderService.failOrder(orderId);
        model.addAttribute("orderId", orderId);
        return "payment/fail";
    }

    @GetMapping("/cancel")
    public String kakaoPayCancel(@RequestParam("orderId") String orderId, Model model) {
        orderService.failOrder(orderId);
        model.addAttribute("orderId", orderId);
        return "payment/cancel";
    }
}