package com.apgroup.app.service;

import com.apgroup.app.dto.CartItemDto;
import com.apgroup.app.entity.CartItem;
import com.apgroup.app.repository.CartItemRepository;
import com.apgroup.app.repository.GoodsRepository;
import com.apgroup.app.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final GoodsRepository goodsRepository;
    private final MemberRepository memberRepository;


    // 장바구니에 추가
    public void addToCart(Long userId, Long goodsId, int quantity) {
        CartItem cartItem = cartItemRepository
                .findByUserIdAndGoodsNo(userId, goodsId)
                .orElseGet(() -> CartItem.builder()
                        .user(memberRepository.findById(userId).orElseThrow())
                        .goods(goodsRepository.findById(goodsId).orElseThrow())
                        .quantity(0)
                        .build());

        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItemRepository.save(cartItem);
    }

    // 장바구니 항목 목록 + 이미지 포함
    public List<CartItemDto> getCartItemDtos(Long userId) {
        return cartItemRepository.findByUserId(userId).stream()
                .map(item -> CartItemDto.builder()
                        .id(item.getId())
                        .goodsNo(item.getGoods().getNo())
                        .gname(item.getGoods().getGname())
                        .img1(item.getGoods().getImg1())
                        .price(item.getGoods().getPrice2())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getQuantity() * item.getGoods().getPrice2())
                        .build())
                .collect(Collectors.toList());
    }

    public void updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow();
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void removeItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void incrementQuantity(Long cartId) {
        CartItem cart = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 항목이 존재하지 않습니다."));

        cart.setQuantity(cart.getQuantity() + 1);
        cartItemRepository.save(cart);
    }

    public void decrementQuantity(Long cartId) {
        CartItem cart = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장바구니 항목이 존재하지 않습니다."));

        if (cart.getQuantity() > 1) {
            cart.setQuantity(cart.getQuantity() - 1);
            cartItemRepository.save(cart);
        }
    }

    // 전체 비우기 기능 추가
    public void clearCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        cartItemRepository.deleteAll(items);
    }

    public void deleteCartItems(List<Long> cartIds) {
        cartItemRepository.deleteAllById(cartIds);
    }

}
