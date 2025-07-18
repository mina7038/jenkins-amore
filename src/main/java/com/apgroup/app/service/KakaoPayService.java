package com.apgroup.app.service;

import com.apgroup.app.dto.KakaoPayResponseDto;
import com.apgroup.app.entity.Order;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

    @Value("${kakaopay.admin-key}")
    private String adminKey;

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.approval-url}")
    private String approvalUrl;

    @Value("${kakaopay.cancel-url}")
    private String cancelUrl;

    @Value("${kakaopay.fail-url}")
    private String failUrl;

    private final OkHttpClient client = new OkHttpClient();
    private static final Logger log = LoggerFactory.getLogger(KakaoPayService.class);

    public KakaoPayResponseDto requestPayment(Order order) {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("cid", cid)
                    .add("partner_order_id", order.getOrderId())
                    .add("partner_user_id", String.valueOf(order.getUser().getId()))
                    .add("item_name", order.getOrderName() != null ? order.getOrderName() : "상품결제")
                    .add("quantity", String.valueOf(order.getQuantity()))
                    .add("total_amount", String.valueOf(order.getTotalAmount()))
                    .add("tax_free_amount", "0")
                    .add("approval_url", approvalUrl + "?orderId=" + order.getOrderId())
                    .add("cancel_url", cancelUrl + "?orderId=" + order.getOrderId())
                    .add("fail_url", failUrl + "?orderId=" + order.getOrderId())
                    .build();

            // ✅ 로그 출력 (stdout X → Logger 사용)
            log.info("📦 카카오페이 결제 요청 준비 중...");
            log.info("→ orderId: {}", order.getOrderId());
            log.info("→ userId: {}", order.getUser().getId());
            log.info("→ orderName: {}", order.getOrderName());
            log.info("→ quantity: {}", order.getQuantity());
            log.info("→ amount: {}", order.getTotalAmount());

            Request request = new Request.Builder()
                    .url("https://kapi.kakao.com/v1/payment/ready")
                    .post(formBody)
                    .addHeader("Authorization", "KakaoAK " + adminKey)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "empty";
                log.error("❌ KakaoPay API 요청 실패: {}", response);
                log.error("❌ KakaoPay 응답 본문: {}", errorBody); // ✅ 서버 로그에 꼭 출력
                throw new IOException("KakaoPay API 요청 실패: " + response);
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            log.info("✅ KakaoPay 응답 수신 완료. redirectUrl: {}", json.getString("next_redirect_pc_url"));

            return KakaoPayResponseDto.builder()
                    .tid(json.getString("tid"))
                    .redirectUrl(json.getString("next_redirect_pc_url"))
                    .build();

        } catch (IOException e) {
            log.error("❌ KakaoPay 요청 실패", e); // ✅ 예외 전체 로그 출력
            throw new RuntimeException("KakaoPay 요청 실패", e);
        }
    }

    public void approvePayment(String pgToken, String orderId) {
        try {
            // 이 부분에서 주문 DB에서 결제 TID 조회 필요
            // 예제에서는 생략 또는 별도 저장했다고 가정

            // KakaoPay 결제 승인 요청 생략 가능 (비즈니스 요구에 따라 결정)
            // 실제 승인 로직 추가 시 KakaoPay API 문서 참고

            // 승인 완료 후 상태 업데이트는 OrderService에서 처리
        } catch (Exception e) {
            throw new RuntimeException("KakaoPay 승인 실패", e);
        }
    }


}
