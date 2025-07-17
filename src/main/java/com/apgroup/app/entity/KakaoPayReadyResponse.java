package com.apgroup.app.entity;

import lombok.Data;

@Data
public class KakaoPayReadyResponse {
    private String tid;
    private String next_redirect_pc_url;
    private String created_at;
}
