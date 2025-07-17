package com.apgroup.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaoPayResponseDto {
    private String tid;
    private String redirectUrl;
}
