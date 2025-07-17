package com.apgroup.app.controller;

import com.apgroup.app.dto.CartItemDto;
import com.apgroup.app.security.CustomUserDetails;
import com.apgroup.app.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 추가
    @PostMapping("/add")
    public String addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                            @RequestParam("goodsId") Long goodsId,
                            @RequestParam("quantity") int quantity) {
        Long userId = userDetails.getMember().getId(); // 로그인 사용자 ID
        cartService.addToCart(userId, goodsId, quantity);
        return "redirect:/cart/list"; // 장바구니 목록 페이지로 리다이렉트
    }

    // 장바구니 목록
    @GetMapping("/list")
    public String cartList(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Long userId = userDetails.getMember().getId();
        List<CartItemDto> cartList = cartService.getCartItemDtos(userId);

        int totalCount = cartList.size();
        int totalPrice = cartList.stream().mapToInt(CartItemDto::getTotalPrice).sum();
        int deliveryFee = totalPrice >= 30000 ? 0 : 3000;
        int finalPrice = totalPrice + deliveryFee;

        model.addAttribute("cartList", cartList);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("finalPrice", finalPrice);

        return "cart/list";
    }

    // 수량 변경
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(@PathVariable("id") Long id,
                                            @RequestParam("quantity") int quantity) {
        cartService.updateQuantity(id, quantity);
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }

    // 항목 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        cartService.removeItem(id);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    @PostMapping("/update")
    public String updateCartQuantity(@RequestParam("cartId") Long cartId,
                                     @RequestParam("action") String action,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        if ("increase".equals(action)) {
            cartService.incrementQuantity(cartId);
        } else if ("decrease".equals(action)) {
            cartService.decrementQuantity(cartId);
        }
        return "redirect:/cart/list";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getMember().getId();
        cartService.clearCart(userId);
        return "redirect:/cart/list";
    }

    @PostMapping("/delete-selected")
    public String deleteSelected(@RequestParam("cartIds") List<Long> cartIds) {
        cartService.deleteCartItems(cartIds);
        return "redirect:/cart/list";
    }


}
