package com.apgroup.app.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.apgroup.app.entity.OrderStatusDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apgroup.app.dto.CartItemSummaryDto;
import com.apgroup.app.entity.*;
import com.apgroup.app.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final GoodsRepository goodsRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;

    public long count() {
        return orderRepository.count(); // JPA 기본 제공 메서드
    }

    public Page<Order> findAllWithMember(Pageable pageable) {
        return orderRepository.findAllWithMember(pageable);
    }

    public Order prepareOrderFromProduct(Long userId, Long goodsId, int quantity, String orderId) {
        Member user = memberRepository.findById(userId).orElseThrow();
        Goods goods = goodsRepository.findById(goodsId).orElseThrow();

        int productTotal = goods.getPrice2() * quantity;
        int shippingFee = productTotal >= 30000 ? 0 : 3000;
        int totalAmount = productTotal + shippingFee; // 📌 배송비 포함

        // 📌 세션에서 배송 정보 꺼내기
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();

        String name = (String) session.getAttribute("shippingName");
        String phone = (String) session.getAttribute("shippingPhone");
        String address = (String) session.getAttribute("shippingAddress");

        String postcode = "";
        String addr1 = "";
        String addr2 = "";

        if (address != null) {
            String[] parts = address.split(" ", 3);
            if (parts.length >= 1) postcode = parts[0];
            if (parts.length >= 2) addr1 = parts[1];
            if (parts.length >= 3) addr2 = parts[2];
        }

        // 주문 객체 생성 및 배송 정보 설정
        Order order = Order.builder()
                .orderId(orderId)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount) // ✅ 배송비 포함한 금액
                .createdAt(LocalDateTime.now())
                .orderName(goods.getGname())
                .quantity(quantity)
                .recipientName(name)
                .phone(phone)
                .postcode(postcode)
                .address1(addr1)
                .address2(addr2)
                .build();

        orderRepository.save(order);

        OrderItem item = OrderItem.builder()
                .order(order)
                .goods(goods)
                .quantity(quantity)
                .unitPrice(goods.getPrice2())
                .totalPrice(productTotal)
                .build();

        orderItemRepository.save(item);
        return order;
    }



    public Order prepareOrderFromCart(Long userId, List<Long> cartItemIds, String orderId) {
        Member user = memberRepository.findById(userId).orElseThrow();
        List<CartItem> selectedItems = cartItemRepository.findAllById(cartItemIds);

        int totalAmount = selectedItems.stream()
                .mapToInt(item -> item.getGoods().getPrice2() * item.getQuantity())
                .sum();

        int shippingFee = totalAmount >= 30000 ? 0 : 3000;
        int finalAmount = totalAmount + shippingFee;

        int totalQuantity = selectedItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        String firstItemName = selectedItems.get(0).getGoods().getGname();
        String orderName = selectedItems.size() == 1
                ? firstItemName
                : firstItemName + " 외 " + (selectedItems.size() - 1) + "건";

        Order.OrderBuilder orderBuilder = Order.builder()
                .orderId(orderId)
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(finalAmount) // ✅ 배송비 포함된 금액 저장
                .shippingFee(shippingFee) // (선택) 엔티티에 shippingFee 필드 있다면
                .createdAt(LocalDateTime.now())
                .orderName(orderName)
                .quantity(totalQuantity)
                .cartItemIds(cartItemIds);

        applyShippingInfoFromSession(orderBuilder);

        Order order = orderBuilder.build();
        orderRepository.save(order);

        for (CartItem cartItem : selectedItems) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .goods(cartItem.getGoods())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getGoods().getPrice2())
                    .totalPrice(cartItem.getGoods().getPrice2() * cartItem.getQuantity())
                    .build();
            orderItemRepository.save(item);
        }

        return order;
    }


    public Order prepareOrderFromWholeCart(Long userId, String orderId) {
        List<CartItem> allItems = cartItemRepository.findByUserId(userId);
        List<Long> ids = allItems.stream().map(CartItem::getId).collect(Collectors.toList());
        return prepareOrderFromCart(userId, ids, orderId);
    }

    public void confirmOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow();

        // 기존 필드 설정
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());

        // 배송비 계산
        int totalAmount = order.getTotalAmount(); // 상품 합계 금액
        int shippingFee = totalAmount >= 30000 ? 0 : 3000;
        int finalAmount = totalAmount + shippingFee;

        // 새 필드 세팅
        order.setShippingFee(shippingFee);
        order.setFinalAmount(finalAmount);

        // 저장
        orderRepository.save(order);

        // 결제 후 장바구니 항목 제거
        List<Long> cartItemIds = order.getCartItemIds();
        if (cartItemIds != null && !cartItemIds.isEmpty()) {
            cartItemRepository.deleteAllById(cartItemIds);
        }
    }

    public void failOrder(String orderId) {
        Order order = orderRepository.findByOrderId(orderId).orElseThrow();
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);
    }

    public List<Order> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public int calculateTotalAmountFromCart(Long userId, List<Long> cartItemIds) {
        Member user = memberRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = cartItemRepository.findByIdInAndUser(cartItemIds, user);
        return cartItems.stream()
                .mapToInt(item -> item.getGoods().getPrice2() * item.getQuantity())
                .sum();
    }

    public int countItemsInCart(Long userId, List<Long> cartItemIds) {
        Member user = memberRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = cartItemRepository.findByIdInAndUser(cartItemIds, user);
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public int calculateTotalAmountFromWholeCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream()
                .mapToInt(item -> item.getGoods().getPrice2() * item.getQuantity())
                .sum();
    }

    public int countItemsInWholeCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public String generateOrderNameFromCart(Long userId, List<Long> cartItemIds) {
        Member user = memberRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = cartItemRepository.findByIdInAndUser(cartItemIds, user);
        if (cartItems.isEmpty()) return "상품결제";

        String firstItemName = cartItems.get(0).getGoods().getGname();
        return cartItems.size() == 1 ? firstItemName : firstItemName + " 외 " + (cartItems.size() - 1) + "건";
    }

    public String generateOrderNameFromWholeCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) return "상품결제";

        String firstItemName = cartItems.get(0).getGoods().getGname();
        return cartItems.size() == 1 ? firstItemName : firstItemName + " 외 " + (cartItems.size() - 1) + "건";
    }

    public List<CartItemSummaryDto> getCartItemSummaries(Long userId, List<Long> cartItemIds) {
        Member user = memberRepository.findById(userId).orElseThrow();
        List<CartItem> cartItems = cartItemRepository.findByIdInAndUser(cartItemIds, user);

        return cartItems.stream()
                .map(item -> new CartItemSummaryDto(
                        item.getGoods().getGname(),
                        item.getQuantity(),
                        item.getGoods().getPrice2(),
                        item.getGoods().getPrice2() * item.getQuantity(),
                        item.getGoods().getImg1(),
                        item.getGoods().getNo()
                ))
                .collect(Collectors.toList());
    }

    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findWithItemsByOrderId(orderId).orElse(null);
    }

    public List<CartItemSummaryDto> getCartItemSummariesFromWholeCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return cartItems.stream().map(cartItem -> {
            Goods goods = goodsRepository.findById(cartItem.getGoods().getNo()).orElse(null);
            if (goods == null) return null;

            int quantity = cartItem.getQuantity();
            int unitPrice = goods.getPrice2();

            return new CartItemSummaryDto(
                    goods.getGname(),
                    quantity,
                    unitPrice,
                    unitPrice * quantity,
                    goods.getImg1(),
                    goods.getNo()
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
    }

    private void applyShippingInfoFromSession(Order.OrderBuilder builder) {
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession();

        String name = (String) session.getAttribute("shippingName");
        String phone = (String) session.getAttribute("shippingPhone");
        String address = (String) session.getAttribute("shippingAddress");

        String postcode = "";
        String addr1 = "";
        String addr2 = "";

        if (address != null) {
            String[] parts = address.split(" ", 3);
            if (parts.length >= 1) postcode = parts[0];
            if (parts.length >= 2) addr1 = parts[1];
            if (parts.length >= 3) addr2 = parts[2];
        }

        builder.recipientName(name)
                .phone(phone)
                .postcode(postcode)
                .address1(addr1)
                .address2(addr2);
    }

    public OrderStatusDto getOrderSummary(Long memberId) {
        OrderStatusDto dto = new OrderStatusDto();

        dto.setDepositPending(orderRepository.countByStatusAndUser_Id(OrderStatus.PENDING, memberId));
        dto.setDeliveryReady(orderRepository.countByDeliveryStatusAndUser_Id(DeliveryStatus.READY, memberId));
        dto.setShipping(orderRepository.countByDeliveryStatusAndUser_Id(DeliveryStatus.SHIPPING, memberId));
        dto.setDelivered(orderRepository.countByDeliveryStatusAndUser_Id(DeliveryStatus.DELIVERED, memberId));


        return dto;
    }

    public List<Order> getRecentOrders(Long memberId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderRepository.findByUserId(memberId, pageable).getContent();
    }

    public List<OrderItem> findItemsWithoutReview(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        // 1. 결제 완료된 주문만 조회
        List<Order> paidOrders = orderRepository.findByUserAndStatus(member, OrderStatus.PAID);


        List<OrderItem> result = new ArrayList<>();

        for (Order order : paidOrders) {
            for (OrderItem item : order.getItems()) {
                boolean hasReview = reviewRepository.existsByMemberAndGoodsAndOrder(member, item.getGoods(), order);
                if (!hasReview) {
                    result.add(item);
                }
            }
        }

        return result;
    }


}
