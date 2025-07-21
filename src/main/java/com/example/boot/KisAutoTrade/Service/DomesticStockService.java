package com.example.boot.KisAutoTrade.Service;

import com.example.boot.KisAutoTrade.Token.RequireValidToken;
import com.example.boot.KisAutoTrade.Token.TokenHolder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Collections;

@Slf4j
@Service
public class DomesticStockService {

    private final TokenHolder tokenHolder;

    public DomesticStockService(TokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @Value("${hantuOpenapi.appkey}")
    private String APP_KEY;
    @Value("${hantuOpenapi.appsecret}")
    private String APP_SECRET;
    @Value("${hantuOpenapi.domain}")
    private String DOMAIN;

    // 주식잔고조회_국내주식
    private final String urlBalance = "/uapi/domestic-stock/v1/trading/inquire-balance";

    /**
     *국내주식잔고조회
     */
    @RequireValidToken
    public void getBalance() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("authorization", "Bearer " + tokenHolder.getAccessToken());
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);
        headers.set("tr_id", "VTTC8434R");

        // 국내주식기간별시세(일/주/월/년)[v1_국내주식-016]
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(DOMAIN + urlBalance)
                .queryParam("CANO", "99969991")// 종합계좌번호
                .queryParam("ACNT_PRDT_CD", "") // 계좌상품코드
                .queryParam("AFHR_FLPR_YN", "N") // 시간외단일가, 거래소여부
                .queryParam("OFL_YN", "") // 오프라인여부
                .queryParam("INQR_DVSN", "01") // 조회구분
                .queryParam("UNPR_DVSN", "01") // 단가구분
                .queryParam("FUND_STTL_ICLD_YN", "Y") // 펀드결제분포함여부
                .queryParam("FNCG_AMT_AUTO_RDPT_YN", "N") // 융자금액자동상환여부
                .queryParam("PRCS_DVSN", "00") // 처리구분
                .queryParam("CTX_AREA_FK100", "") // 연속조회검색조건 100
                .queryParam("CTX_AREA_NK100", ""); // 연속조회키 100

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);

        log.info(" response.getBody(): {}",  response.getBody());

    }

}
