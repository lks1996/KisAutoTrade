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

        /** 1. 보유하고 있지 않은 종목이 있다면, 해당 종목을 먼저 구매함. */

        List<SheetDto> unholdingStockList = sheetList.stream()
                .filter(sheet -> sbrDto.getOutput1().stream()
                        .noneMatch(own -> own.getPdno().equals(sheet.getStockCode())))
                .toList();

        if (!unholdingStockList.isEmpty()) {
//            long cashBalance = Long.parseLong(sbrDto.getOutput2().get(0).getDncaTotAmt());
            long cashBalance = 1000000;

            double sumRatioOfHoldings = sheetList.stream()
                    .filter(p -> holdingStocks.stream().anyMatch(h -> h.getPdno().equals(p.getStockCode())))
                    .mapToDouble(SheetDto::getTargetRatio)
                    .sum();

            double sumRatioOfUnholdings = 100.0 - sumRatioOfHoldings;

            List<StockDto> toBuyList = new ArrayList<>();

            List<Map<String, Object>> unholdingBuyCalcList = new ArrayList<>();


            for (SheetDto p : unholdingStockList) {
                double ratioInUnholdings = p.getTargetRatio() / sumRatioOfUnholdings;
                long needToBuyAmount = Math.round(cashBalance * ratioInUnholdings);

                StockDto stockPriceDto = new StockDto();
                stockPriceDto.setFidInputIscd(p.getStockCode());

                JsonNode rootNode = mapper.readTree(domesticStockService.getDomesticStockPrice(stockPriceDto));
                JsonNode outputNode = rootNode.path("output");
                StockPriceResponseDto sprDto = mapper.treeToValue(outputNode, StockPriceResponseDto.class);

                long stockPrice = Long.parseLong(sprDto.getStckPrpr());

                long quantityToBuy = 0;
                if (stockPrice > 0 && needToBuyAmount >= stockPrice) {
                    quantityToBuy = needToBuyAmount / stockPrice; // 정상 수량 계산
                }

                if (quantityToBuy > 0) {
                    toBuyList.add(StockDto.builder()
                            .pdno(p.getStockCode())
                            .ordUnpr(String.valueOf(stockPrice))
                            .ordQty(String.valueOf(quantityToBuy))
                            .build()
                    );
                }

                // 결과 저장용 리스트
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("code", p.getStockCode());
                resultMap.put("name", p.getStockName());
                resultMap.put("needToBuyAmount", needToBuyAmount);
                resultMap.put("price", stockPrice);
                resultMap.put("qty", quantityToBuy);
                unholdingBuyCalcList.add(resultMap);
            }
            log.info("===미보유 종목 존재===");

            // 결과 출력 종목명, 종목코드, 구매금액, 실제 구매비율
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

            // 매수 로직 필요.

        }


        /** 2. 이후에 보유 종목에 대해 포트폴리오 비율과 비교하여 추가 매수 진행.(보유 종목의 현재 비율은 내림 처리.) */



        // 4. 주문 실행
        StockDto stockOrderDto = new StockDto();
//        domesticStockService.orderDomesticStockCash(stockOrderDto);
    }
}
