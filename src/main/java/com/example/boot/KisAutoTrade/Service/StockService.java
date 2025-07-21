package com.example.boot.KisAutoTrade.Service;

import com.example.boot.KisAutoTrade.Entity.Stock;
import com.example.boot.KisAutoTrade.Repository.StockRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Service
//@Slf4j
public class StockService {

//    // https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations2#L_a08c3421-e50f-4f24-b1fe-64c12f723c77 // 한투 openapi 문서
//    // https://www.ktb.co.kr/trading/popup/itemPop.jspx // 종목코드 검색
//
//    @Autowired
//    private StockRepository stockRepository;
//
//    @Value("${hantu-openapi.domain}")
//    private String apiDomain;
//
//    @Value("${hantu-openapi.appkey}")
//    private String appKey;
//
//    @Value("${hantu-openapi.appsecret}")
//    private String appSecret;
//
//    // api 받아와서 db에 저장
//    public void fetchAndSaveStockData(String marketCode, String stockCode, String startDate, String endDate, String periodCode) {
//        TokenResponse accessToken = getAccessToken();
//        String responseBody = getStockPriceData(marketCode, stockCode, startDate, endDate, periodCode, accessToken.getAccess_token());
//        //log.info("responseBody: " + responseBody);
//        saveStockData(responseBody, stockCode);
//    }
//
//    // api 데이터 받아옴
//    private String getStockPriceData(String marketCode, String stockCode, String startDate, String endDate, String periodCode, String accessToken) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        headers.set("authorization", "Bearer " + accessToken);
//        headers.set("appkey", appKey);
//        headers.set("appsecret", appSecret);
//        headers.set("tr_id", "FHKST03010100");
//
//        // 국내주식기간별시세(일/주/월/년)[v1_국내주식-016]
//        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(apiDomain + "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
//                .queryParam("FID_COND_MRKT_DIV_CODE", marketCode)
//                .queryParam("FID_INPUT_ISCD", stockCode)
//                .queryParam("FID_INPUT_DATE_1", startDate)
//                .queryParam("FID_INPUT_DATE_2", endDate)
//                .queryParam("FID_PERIOD_DIV_CODE", periodCode)
//                .queryParam("FID_ORG_ADJ_PRC", "0");
//
//        HttpEntity<?> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(
//                builder.toUriString(),
//                HttpMethod.GET,
//                entity,
//                String.class);
//
//        return response.getBody();
//    }
//
//    // db에 저장함
//    private void saveStockData(String responseBody, String stockCode) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode rootNode = mapper.readTree(responseBody);
//            JsonNode output2 = rootNode.get("output2");
//
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//
//            for (JsonNode node : output2) {
//                Stock stock = new Stock();
//                stock.setStockCode(stockCode);
//                stock.setDate(LocalDate.parse(node.get("stck_bsop_date").asText(), formatter));
//                stock.setMaxPrice(node.get("stck_hgpr").asText());
//                stock.setMinPrice(node.get("stck_lwpr").asText());
//                stock.setAccumTrans(node.get("acml_vol").asText());
//                stock.setOpenPrice(node.get("stck_oprc").asText());
//                stock.setClosePrice(node.get("stck_clpr").asText());
//
//                stockRepository.save(stock);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            // 에러 처리 로직 추가
//        }
//    }
//
//    public TokenResponse getAccessToken() {
//
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        String requestBody = String.format(
//                "{\"grant_type\":\"client_credentials\",\"appkey\":\"%s\",\"appsecret\":\"%s\"}",
//                appKey, appSecret
//        );
//
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<TokenResponse> response = restTemplate.exchange(
//                apiDomain + "/oauth2/tokenP",
//                HttpMethod.POST,
//                entity,
//                TokenResponse.class
//        );
//
//        if (response.getStatusCode() == HttpStatus.OK) {
//            return response.getBody();
//        } else {
//            throw new RuntimeException("Failed to obtain access token");
//        }
//    }

}
