package com.example.boot.KisAutoTrade.Service;

import com.example.boot.KisAutoTrade.DTO.StockDto;
import com.example.boot.KisAutoTrade.Token.RequireValidToken;
import com.example.boot.KisAutoTrade.Token.TokenHolder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DomesticStockService {

    private final TokenHolder tokenHolder;

    public DomesticStockService(TokenHolder tokenHolder) {

        this.tokenHolder = tokenHolder;
    }

    @Value("${spring.profiles.active}")
    private String profile;
    @Value("${hantuOpenapi.appkey}")
    private String APP_KEY;
    @Value("${hantuOpenapi.appsecret}")
    private String APP_SECRET;
    @Value("${hantuOpenapi.domain}")
    private String DOMAIN;
    @Value("${hantuOpenapi.cano}")
    private String CANO;
    @Value("${hantuOpenapi.acntprdtcd}")
    private String ACNT_PRDT_CD;

    // 주식잔고조회_국내주식
    private final String urlBalance = "/uapi/domestic-stock/v1/trading/inquire-balance";
    // 주식현재가 시세_국내주식
    private final String urlInquirePrice = "/uapi/domestic-stock/v1/quotations/inquire-price";
    // 국내주식주문(현금)_국내주식
    private final String urlOrder = "/uapi/domestic-stock/v1/trading/order-cash";

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("authorization", "Bearer " + tokenHolder.getAccessToken());
        headers.set("appkey", APP_KEY);
        headers.set("appsecret", APP_SECRET);

        return headers;
    }

    /**
     * 국내주식잔고조회
     *
     * @return
     */
    @RequireValidToken
    public String getBalance(StockDto stockDto) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHttpHeaders();

        if(profile.equals("prod")) {
            headers.set("tr_id", "TTTC8434R");  // 실전용
        } else {
            headers.set("tr_id", "VTTC8434R");  // 모의용
        }

        // 주식잔고조회[v1_국내주식-006]
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(DOMAIN + urlBalance)
                .queryParam("CANO", CANO)                                               // 종합계좌번호
                .queryParam("ACNT_PRDT_CD", ACNT_PRDT_CD)                               // 계좌상품코드
                .queryParam("AFHR_FLPR_YN", stockDto.getAfhrFlprYn())                   // 시간외단일가, 거래소여부
                .queryParam("OFL_YN", stockDto.getOflYn())                              // 오프라인여부
                .queryParam("INQR_DVSN", stockDto.getInorDvsn())                        // 조회구분
                .queryParam("UNPR_DVSN", stockDto.getUnprDvsn())                        // 단가구분
                .queryParam("FUND_STTL_ICLD_YN", stockDto.getFundSttlIcldYn())          // 펀드결제분포함여부
                .queryParam("FNCG_AMT_AUTO_RDPT_YN", stockDto.getFncgAmtAutoRdptYn())   // 융자금액자동상환여부
                .queryParam("PRCS_DVSN", stockDto.getPrcsDvsn())                        // 처리구분
                .queryParam("CTX_AREA_FK100", stockDto.getCtxAreaFk100())               // 연속조회검색조건 100
                .queryParam("CTX_AREA_NK100", stockDto.getCtxAreaNk100());              // 연속조회키 100

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);

        log.info(" response.getBody(): {}",  response.getBody());
        log.info("[DomesticService.getBalance succeed.]");

        return response.getBody();
    }

    /**
     * 국내주식현재가 시세
     */
    @RequireValidToken
    public void getDomesticStockPrice(StockDto orderStock) {

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHttpHeaders();

        headers.set("custtype", "P");// 고객 타입 (B: 법인 , P: 개인)
        if(profile.equals("prod")) {
            headers.set("tr_id", "FHKST01010100");  // 실전용
        } else {
            headers.set("tr_id", "FHKST01010100");  // 모의용
        }

        // 주식현재가 시세[v1_국내주식-008]
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(DOMAIN + urlInquirePrice)
                .queryParam("FID_COND_MRKT_DIV_CODE", orderStock.getFidCondMrktDivCode())   // 조건 시장 분류 코드( J:KRX, NX:NXT, UN:통합 )
                .queryParam("FID_INPUT_ISCD", orderStock.getFidInputIscd());                // 입력 종목코드 (ex 005930 삼성전자)

        HttpEntity<?> entity = new HttpEntity<>(headers);

        log.info("==========================================");
        log.info(" 조건 시장 분류 코드: {}", orderStock.getFidCondMrktDivCode());
        log.info(" 입력 종목코드: {}", orderStock.getFidInputIscd());
        log.info("==========================================");

        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);

        log.info(" response.getBody(): {}",  response.getBody());
        log.info("[DomesticService.getDomesticStockPrice succeed.]");
    }

    /**
     * 국내주식주문(현금)
     */
    @RequireValidToken
    public void orderDomesticStockCash(StockDto orderStock) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = getHttpHeaders();

        headers.set("custtype", "P");// 고객 타입 (B: 법인 , P: 개인)
        if(profile.equals("prod")) {
            if(orderStock.getOrderType() == 1) {
                headers.set("tr_id", "TTTC0011U");  // 실전용(매도)
            } else if (orderStock.getOrderType() == 2) {
                headers.set("tr_id", "TTTC0012U");  // 실전용(매수)
            }
        } else if(profile.equals("dev")){
            if(orderStock.getOrderType() == 1) {
                headers.set("tr_id", "VTTC0011U");  // 모의용(매도)
            } else if (orderStock.getOrderType() == 2) {
                headers.set("tr_id", "VTTC0012U");  // 모의용(매수)
            }
        }

        // 주식주문(현금)[v1_국내주식-001]
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("CANO", CANO);
        requestBody.put("ACNT_PRDT_CD", ACNT_PRDT_CD);
        requestBody.put("PDNO", orderStock.getPdno());          // 종목코드
        requestBody.put("ORD_DVSN", orderStock.getOrdDvsn());   // 주문구분
        requestBody.put("ORD_QTY", orderStock.getOrdQty());     // 주문수량
        requestBody.put("ORD_UNPR", orderStock.getOrdUnpr());   // 주문단가


        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        log.info("==========================================");
        log.info(" 주문타입: {}",  orderStock.getOrderType() == 1 ? "매도":"매수");
        log.info(" 종목코드: {}",  orderStock.getPdno());
        log.info(" 주문수량: {}",  orderStock.getOrdQty());
        log.info(" 주문단가: {}",  orderStock.getOrdUnpr());
        log.info("==========================================");

        log.info("Headers: {}", headers);
        log.info("Request Body: {}", new ObjectMapper().writeValueAsString(requestBody));

        ResponseEntity<String> response = restTemplate.exchange(
                DOMAIN + urlOrder,
                HttpMethod.POST,
                entity,
                String.class);

        log.info(" response.getBody(): {}",  response.getBody());
        log.info("[DomesticService.orderDomesticStockCash succeed.]");
    }
}
