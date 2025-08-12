package com.example.boot.KisAutoTrade.Service;

import com.example.boot.KisAutoTrade.DTO.Response.BalanceOutput1Dto;
import com.example.boot.KisAutoTrade.DTO.Response.SheetDto;
import com.example.boot.KisAutoTrade.DTO.Response.StockBalanceResponseDto;
import com.example.boot.KisAutoTrade.DTO.Request.StockDto;
import com.example.boot.KisAutoTrade.DTO.Response.StockPriceResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AutoTradeService {

    private final DomesticStockService domesticStockService;
    private final SheetDataImportService sheetDataImportService;

    public AutoTradeService(DomesticStockService domesticStockService,  SheetDataImportService sheetDataImportService) {
        this.domesticStockService = domesticStockService;
        this.sheetDataImportService = sheetDataImportService;
    }

    public void execute() throws Exception {
        // 1. 구글 시트에서 목표 비중 가져오기
        List<List<Object>> sheetDataList = sheetDataImportService.getSheetsData();

        List<SheetDto> sheetList = new ArrayList<>();

        for (int i = 0; i < sheetDataList.size(); i++) { // i=1부터 시작: 첫 줄은 헤더
            List<Object> row = sheetDataList.get(i);

//            String accountType = row.size() > 0 ? row.get(0).toString() : "";
//            String stockType1 = row.size() > 1 ? row.get(1).toString() : "";
            double categoryTargetRatio = row.size() > 0 ? Double.parseDouble(row.get(0).toString()) : 0;
            String stockType2 = row.size() > 1 ? row.get(1).toString() : "";
            String stockCode = row.size() > 2 ? row.get(2).toString() : "";
            String stockName = row.size() > 3 ? row.get(3).toString() : "";
            double targetRatio = row.size() > 4 ? Double.parseDouble(row.get(4).toString()) : 0;

            SheetDto dto = new SheetDto("", "", stockType2, stockCode,  stockName, categoryTargetRatio, targetRatio);
            sheetList.add(dto);
        }

        // 2. 잔고 조회
        StockDto requestDto = new StockDto();
        String balancerResponse = domesticStockService.getBalance(requestDto);

        ObjectMapper mapper = new ObjectMapper();
        StockBalanceResponseDto sbrDto = mapper.readValue(balancerResponse, StockBalanceResponseDto.class);

        if( sbrDto.getOutput1()==null || sbrDto.getOutput2()==null ){
            log.error("[ERROR] 잔고 조회 실패. output1 혹은 output2가 null.");
            return;
        }

        List<BalanceOutput1Dto> holdingStocks = sbrDto.getOutput1();

        log.info("예수금 총액: {}", sbrDto.getOutput2().get(0).getDncaTotAmt());

        /** 1. 보유하고 있지 않은 종목이 있다면, 해당 종목을 먼저 구매함.(보유 중인 종목은 지정된 비율만큼 이미 보유하고 있다고 가정.) */

        // 미보유 종목 추출.
        StockBalanceResponseDto finalSbrDto = sbrDto;
        List<SheetDto> unholdingStockList = sheetList.stream()
                .filter(sheet -> finalSbrDto.getOutput1().stream()
                        .noneMatch(own -> own.getPdno().equals(sheet.getStockCode())))
                .toList();

        // 미보유 종목이 존재한다면,
        if (!unholdingStockList.isEmpty()) {
            // 보유 중인 예치금 조회.
//            long cashBalance = Long.parseLong(sbrDto.getOutput2().get(0).getDncaTotAmt());
            long cashBalance = 1000000;

            // 보유 중인 종목은 포트폴리오의 비율만큼 이미 가지고 있다고 가정.
            List<BalanceOutput1Dto> finalHoldingStocks = holdingStocks;
            double sumRatioOfHoldings = sheetList.stream()
                    .filter(p -> finalHoldingStocks.stream().anyMatch(h -> h.getPdno().equals(p.getStockCode())))
                    .mapToDouble(SheetDto::getTargetRatio)
                    .sum();

            // 미보유 종목의 총 구매 필요 비율 계산.
            double sumRatioOfUnholdings = 100.0 - sumRatioOfHoldings;

            List<StockDto> toBuyList = new ArrayList<>();
            List<Map<String, Object>> unholdingBuyCalcList = new ArrayList<>();

            for (SheetDto p : unholdingStockList) {
                double ratioInUnholdings = p.getTargetRatio() / sumRatioOfUnholdings;   // 각 미보유 종목의 구매 필요 비율 계산.
                long needToBuyAmount = Math.round(cashBalance * ratioInUnholdings);     // 구매 필요 비율에 따른 구매 필요 금액 계산.

                // 현재가 조회를 위한 세팅.
                StockDto stockPriceDto = new StockDto();
                stockPriceDto.setFidInputIscd(p.getStockCode());

                // 현재가 조회 결과를 StockPriceResponseDto에 매핑.
                JsonNode rootNode = mapper.readTree(domesticStockService.getDomesticStockPrice(stockPriceDto));
                JsonNode outputNode = rootNode.path("output");
                StockPriceResponseDto sprDto = mapper.treeToValue(outputNode, StockPriceResponseDto.class);

                long stockPrice = Long.parseLong(sprDto.getStckPrpr()); // 현재가 세팅.
                long quantityToBuy = 0;                                 // 구매 수량 디폴트 0 세팅.

                if (stockPrice > 0 && needToBuyAmount >= stockPrice) {
                    quantityToBuy = needToBuyAmount / stockPrice; // 구매 필요 수량 계산.
                }

                // 구매 필요 수량이 있는 경우 리스트에 추가.
                if (quantityToBuy > 0) {
                    toBuyList.add(StockDto.builder()
                            .pdno(p.getStockCode())
                            .ordUnpr(String.valueOf(stockPrice))
                            .ordQty(String.valueOf(quantityToBuy))
                            .build()
                    );
                }

                // 결과 저장용 리스트.
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("code", p.getStockCode());
                resultMap.put("name", p.getStockName());
                resultMap.put("needToBuyAmount", needToBuyAmount);
                resultMap.put("price", stockPrice);
                resultMap.put("qty", quantityToBuy);
                unholdingBuyCalcList.add(resultMap);
            }
            log.info("===미보유 종목 존재===");

            // 결과 출력 ( 종목명, 종목코드, 구매금액, 실제 구매비율 )
            long sumNeedToBuyAmount = unholdingBuyCalcList.stream()
                    .mapToLong(m -> (long) m.get("needToBuyAmount"))
                    .sum();

            for (Map<String, Object> res : unholdingBuyCalcList) {
                double buyRatio = sumNeedToBuyAmount > 0
                        ? ((long) res.get("needToBuyAmount") * 100.0) / sumNeedToBuyAmount
                        : 0.0;
                res.put("buyRatio", buyRatio);

                log.info("미보유 종목 {}({}): 구매금액 {}원, 실제 구매비율 {}%",
                        res.get("name"),
                        res.get("code"),
                        res.get("needToBuyAmount"),
                        buyRatio
                );
            }

            // 매수 로직 필요.( toBuyList )

        }


        /** 2. 이후에 보유 종목에 대해 포트폴리오 비율과 비교하여 추가 매수 진행.(보유 종목의 현재 비율은 내림 처리.) */

        // 1. 잔고 재조회.
        balancerResponse = domesticStockService.getBalance(requestDto);
        sbrDto = mapper.readValue(balancerResponse, StockBalanceResponseDto.class);

        holdingStocks = sbrDto.getOutput1();                                            // 미보유 매수 후 현재 보유 중인 종목 확인.
        long remainCash = Long.parseLong(sbrDto.getOutput2().get(0).getDncaTotAmt());   // 미보유 매수 후 남은 예수금 확인.

        // 2. 총 평가 금액 ( 보유 종목 평가금 )
        long totalEvalAmount = holdingStocks.stream()
                .mapToLong(h -> Long.parseLong(h.getEvluAmt()))
                .sum();

        // 3. 비율 비교 후 부족분 매수
        List<StockDto> rebalanceBuyList = new ArrayList<>();

        for (BalanceOutput1Dto holding : holdingStocks) {
            String stockCode = holding.getPdno();                   // 종목 코드 세팅.
            long evalAmt = Long.parseLong(holding.getEvluAmt());    // 평가 금액 세팅.

            // 포트폴리오에서 목표 비율 추출.
            double targetRatio = sheetList.stream()
                    .filter(s -> s.getStockCode().equals(stockCode))
                    .mapToDouble(SheetDto::getTargetRatio)
                    .findFirst()
                    .orElse(0.0);

            // 현재 비율 계산
            double currentRatio = (evalAmt / (double) totalEvalAmount) * 100.0;

            // 목표 비중보다 현재 종목의 비율이 작다면,
            if (currentRatio < targetRatio) {

                double shortageRatio = targetRatio - currentRatio;                              // 부족 비율 계산.
                long shortageAmount = Math.round(totalEvalAmount * (shortageRatio / 100.0));    // 부족 금액 계산.

                // 현재가 조회를 위한 세팅.
                StockDto stockPriceDto = new StockDto();
                stockPriceDto.setFidInputIscd(stockCode);

                // 현재가 조회 결과를 StockPriceResponseDto에 매핑.
                JsonNode rootNode = mapper.readTree(domesticStockService.getDomesticStockPrice(stockPriceDto));
                JsonNode outputNode = rootNode.path("output");
                StockPriceResponseDto sprDto = mapper.treeToValue(outputNode, StockPriceResponseDto.class);

                long stockPrice = Long.parseLong(sprDto.getStckPrpr()); // 현재가 세팅.

                // 매수 수량 계산 ( 0주도 허용 )
                long quantityToBuy = shortageAmount / stockPrice;

                // 수량이 1 이상일 때만 매수 리스트에 추가
                if (quantityToBuy > 0) {
                    rebalanceBuyList.add(StockDto.builder()
                            .pdno(stockCode)
                            .ordUnpr(String.valueOf(stockPrice))
                            .ordQty(String.valueOf(quantityToBuy))
                            .build()
                    );
                }

                remainCash -= (quantityToBuy * stockPrice);
                log.info("보유 종목 비율 조정 매수: {} ({}주, {}원)", stockCode, quantityToBuy, quantityToBuy * stockPrice);
                }
            }
        }

        // rebalanceBuyList에 담긴 내용 매수 실행 로직 추가


        // 4. 주문 실행
        StockDto stockOrderDto = new StockDto();
//        domesticStockService.orderDomesticStockCash(stockOrderDto);
    }
