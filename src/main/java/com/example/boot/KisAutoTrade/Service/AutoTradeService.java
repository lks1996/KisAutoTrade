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
        /// FOR TEST ///
        long cashBalance = 1000000;
        long totalUnholdingBuyAmount = 0;
        /// FOR TEST ///



        /** 1. 구글 시트에서 목표 비중 가져오기 */
        List<List<Object>> sheetDataList = sheetDataImportService.getSheetsData();
        List<SheetDto> sheetList = parseSheetData(sheetDataList);

        /** 2. 잔고 조회 */
        StockDto requestDto = new StockDto();
        String balancerResponse = domesticStockService.getBalance(requestDto);

        ObjectMapper mapper = new ObjectMapper();
        StockBalanceResponseDto sbrDto = mapper.readValue(balancerResponse, StockBalanceResponseDto.class);

        if( sbrDto.getOutput1()==null || sbrDto.getOutput2()==null ){
            log.error("[ERROR] 잔고 조회 실패. output1 혹은 output2가 null.");
            return;
        }

        List<BalanceOutput1Dto> holdingStocks = sbrDto.getOutput1();

        log.info("실예수금 총액: {}", sbrDto.getOutput2().get(0).getDncaTotAmt());
        log.info("테스트 예수금 총액: {}", cashBalance);

        /** 3. 보유하고 있지 않은 종목이 있다면, 해당 종목을 먼저 구매함.(단, 보유 중인 종목은 지정된 비율만큼 이미 보유하고 있다고 가정.) */
        // 3-1. 미보유 종목 추출.( 미보유 종목이더라도 목표비중이 0이라면 제외함. )
        StockBalanceResponseDto finalSbrDto = sbrDto;
        List<SheetDto> unholdingStockList = sheetList.stream()
                .filter(sheet -> sheet.getTargetRatio() > 0)
                .filter(sheet -> finalSbrDto.getOutput1().stream()
                        .noneMatch(own -> own.getPdno().equals(sheet.getStockCode())))
                .toList();

        // 미보유 종목이 존재한다면,
        if (!unholdingStockList.isEmpty()) {
            // 3-2. 보유 중인 예수금 조회.
//            long cashBalance = Long.parseLong(sbrDto.getOutput2().get(0).getDncaTotAmt());

            // 3-3. 보유 중인 종목은 포트폴리오의 비율만큼 이미 가지고 있다고 가정.
            List<BalanceOutput1Dto> finalHoldingStocks = holdingStocks;
//            double sumRatioOfHoldings = sheetList.stream()
//                    .filter(p -> finalHoldingStocks.stream().anyMatch(h -> h.getPdno().equals(p.getStockCode())))
//                    .mapToDouble(SheetDto::getTargetRatio)
//                    .sum();
//
//            // 3-4. 미보유 종목의 총 구매 필요 비율 계산.
//            double sumRatioOfUnholdings = 100.0 - sumRatioOfHoldings;
//            List<StockDto> toBuyList = calculateUnholdingBuys(unholdingStockList, sumRatioOfUnholdings, cashBalance);

            // 포트폴리오 총액 = 보유 종목 평가금액 합 + 예수금
            long portfolioTotal = holdingStocks.stream()
                    .mapToLong(h -> Long.parseLong(h.getEvluAmt()))
                    .sum() + cashBalance;

            List<StockDto> toBuyList = calculateUnholdingBuys(unholdingStockList, portfolioTotal);


            /// FOR TEST ///
            // 미보유 종목 구매 총액 계산
            totalUnholdingBuyAmount = toBuyList.stream()
                    .mapToLong(stock -> {
                        long price = Long.parseLong(stock.getOrdUnpr());
                        long qty = Long.parseLong(stock.getOrdQty());
                        return price * qty;
                    })
                    .sum();
            log.info("테스트 예수금 총액 - 미보유 종목 구매 금액: {}", cashBalance - totalUnholdingBuyAmount);
            /// FOR TEST ///


            // 3-5. 추가 매수가 필요한 종목 주문.
            orderStocks(toBuyList);
        }


        /** 4. 이후에 보유 종목에 대해 포트폴리오 비율과 비교하여 추가 매수 진행.(보유 종목의 현재 비율은 내림 처리.) */
        // 4-1. 잔고 재조회.
        balancerResponse = domesticStockService.getBalance(requestDto);
        sbrDto = mapper.readValue(balancerResponse, StockBalanceResponseDto.class);

        holdingStocks = sbrDto.getOutput1();                                            // 미보유 매수 후 현재 보유 중인 종목 확인.
//        long cashBalance = Long.parseLong(sbrDto.getOutput2().get(0).getDncaTotAmt());   // 미보유 매수 후 남은 예수금 확인.
//        long portfolioTotal = holdingStocks.stream()
//                .mapToLong(h -> Long.parseLong(h.getEvluAmt()))
//                .sum() + cashBalance;

        /// FOR TEST ///
        long remainCash = cashBalance -  totalUnholdingBuyAmount;
        /// FOR TEST ///

        // 4-2. 추가 매수 필요 리스트 추출.
        List<StockDto> rebalanceBuyList = calculateRebalanceBuys(holdingStocks, sheetList, remainCash);


        /// FOR TEST ///
        long totalRevaluncingAmount = rebalanceBuyList.stream()
                .mapToLong(stock -> {
                    long price = Long.parseLong(stock.getOrdUnpr());
                    long qty = Long.parseLong(stock.getOrdQty());
                    return price * qty;
                })
                .sum();
        log.info("테스트 예수금 총액 - 미보유 종목 구매 금액 - 리밸런싱 종목 구매 금액: {}", cashBalance - totalUnholdingBuyAmount - totalRevaluncingAmount);
        /// FOR TEST ///



        // 4-3. 추가 매수가 필요한 종목 주문.
        orderStocks(rebalanceBuyList);

        log.info("==========================");
        log.info("자동 매수 처리 완료.");
        log.info("==========================");
    }


    /**
     * 포트폴리오 데이터 임포트.
     * @param sheetDataList 구글 시트 전체 데이터 목록
     * @return resultList 포트폴리오 목록
     * @throws Exception
     */
    private List<SheetDto> parseSheetData(List<List<Object>> sheetDataList) throws Exception {

        List<SheetDto> resultList = new ArrayList<>();

        for (int i = 0; i < sheetDataList.size(); i++) { // i=1부터 시작: 첫 줄은 헤더
            List<Object> row = sheetDataList.get(i);

//            String accountType = row.size() > 0 ? row.get(0).toString() : "";
//            String stockType1 = row.size() > 1 ? row.get(1).toString() : "";
            double categoryTargetRatio = row.size() > 0 ? Double.parseDouble(row.get(0).toString()) : 0;
            String stockType2 = row.size() > 1 ? row.get(1).toString() : "";
            String stockCode = row.size() > 2 ? row.get(2).toString() : "";
            String stockName = row.size() > 3 ? row.get(3).toString() : "";
            double targetRatio = row.size() > 4 ? Double.parseDouble(row.get(4).toString()) : 0;

            resultList.add(new SheetDto("", "", stockType2, stockCode, stockName, categoryTargetRatio, targetRatio));
        }
        return resultList;
    }

    /**
     * 현재가 조회.
     * @param stockCode 종목코드
     * @return resultDto 현재 종목 정보
     * @throws Exception
     */
    private StockPriceResponseDto getCurrentStockPrice(String stockCode) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // 현재가 조회를 위한 세팅.
        StockDto stockPriceDto = new StockDto();
        stockPriceDto.setFidInputIscd(stockCode);

        // 현재가 조회 결과를 StockPriceResponseDto에 매핑.
        JsonNode rootNode = mapper.readTree(domesticStockService.getDomesticStockPrice(stockPriceDto));
        JsonNode outputNode = rootNode.path("output");
        StockPriceResponseDto resultDto = mapper.treeToValue(outputNode, StockPriceResponseDto.class);

        return resultDto;
    }

    /**
     * 미보유 종목 매수 계산.
     * @param unholdingStockList 미보유 종목 목록
     * @param portfolioTotal 총 보유 종목 평가금 + 현금 예수금
     * @return resultList 매수 대상 종목 리스트
     * @throws Exception
     */
    private List<StockDto> calculateUnholdingBuys(List<SheetDto> unholdingStockList, long portfolioTotal) throws Exception {

        List<StockDto> resultList = new ArrayList<>();
        List<Map<String, Object>> resultLogList = new ArrayList<>();

        for (SheetDto p : unholdingStockList) {
            // 목표 금액 = 포트폴리오 총액 * 목표 비중
            long targetAmount = (long)Math.floor(portfolioTotal * (p.getTargetRatio() / 100.0));

            // 미보유 종목이므로 현재 보유금액은 0
            long currentHoldingAmount = 0L;

            // 부족분 = 목표 금액 - 현재 보유 금액
            long needToBuyAmount = targetAmount - currentHoldingAmount;

            // 현재가 조회
            StockPriceResponseDto sprDto = getCurrentStockPrice(p.getStockCode());
            long stockPrice = Long.parseLong(sprDto.getStckPrpr());

            long quantityToBuy = 0;
            if (stockPrice > 0 && needToBuyAmount >= stockPrice) {
                quantityToBuy = needToBuyAmount / stockPrice;
            }

            // 구매 필요 수량이 있는 경우 리스트에 추가
            if (quantityToBuy > 0) {
                resultList.add(StockDto.builder()
                        .pdno(p.getStockCode())
                        .ordUnpr(String.valueOf(stockPrice))
                        .ordQty(String.valueOf(quantityToBuy))
                        .orderType(2)
                        .ordDvsn("00")
                        .build()
                );
            }

            // 결과 저장용 로그
            Map<String, Object> resultLogMap = new HashMap<>();
            resultLogMap.put("code", p.getStockCode());
            resultLogMap.put("name", p.getStockName());
            resultLogMap.put("needToBuyAmount", needToBuyAmount);
            resultLogMap.put("price", stockPrice);
            resultLogMap.put("qty", quantityToBuy);
            resultLogList.add(resultLogMap);
        }

        log.info("===미보유 종목 존재===");

        // 결과 출력 ( 종목명, 종목코드, 구매금액, 실제 구매비율 )
        long sumNeedToBuyAmount = resultLogList.stream()
                .mapToLong(m -> (long) m.get("needToBuyAmount"))
                .sum();

        for (Map<String, Object> res : resultLogList) {
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

        return resultList;
    }

    /**
     * 보유 종목 중 추가 매수 필요 종목 계산.
     * @param holdingStocks 현재 보유 종목 목록
     * @param sheetList 포트폴리오 목표 비중 목록
     * @param remainCash 현재 예수금
     * @return resultList 매수 대상 종목 리스트
     * @throws Exception
     */
    private List<StockDto> calculateRebalanceBuys(List<BalanceOutput1Dto> holdingStocks, List<SheetDto> sheetList, long remainCash) throws Exception {
        // 1. 총 평가 금액 계산. (보유 종목 평가금)
        long totalEvalAmount = holdingStocks.stream()
                .mapToLong(h -> Long.parseLong(h.getEvluAmt()))
                .sum() + remainCash;

        // 2. 비율 비교 후 부족분 매수.
        List<StockDto> resultList = new ArrayList<>();

        for (BalanceOutput1Dto holding : holdingStocks) {
            if (remainCash <= 0) {
                log.info("예수금 소진으로 추가 매수 중단");
                break;
            }

            String stockCode = holding.getPdno();
            long evalAmt = Long.parseLong(holding.getEvluAmt());

            // 포트폴리오 목표 비율 추출.
            double targetRatio = sheetList.stream()
                    .filter(s -> s.getStockCode().equals(stockCode))
                    .mapToDouble(SheetDto::getTargetRatio)
                    .findFirst()
                    .orElse(0.0);

            // 현재 비율 계산.
            double currentRatio = (evalAmt / (double) totalEvalAmount) * 100.0;

            // 목표 비중 보다 낮을 때만 매수 진행.
            if (currentRatio < targetRatio) {
                double shortageRatio = targetRatio - currentRatio;                              // 목표비중 - 현재비중
                long shortageAmount = Math.round(totalEvalAmount * (shortageRatio / 100.0));    // 목표 비중에 도달하기 위해 필요한 추가 매수 금액.

                // 잔액 초과 방지.( 추가 매수 필요 금액보다 예수금이 적은 경우 예수금에 맞게 매수되도록 금액 조정. )
                if (shortageAmount > remainCash) {
                    shortageAmount = remainCash;
                }

                // 현재가 조회.
                StockPriceResponseDto sprDto = getCurrentStockPrice(stockCode);
                long stockPrice = Long.parseLong(sprDto.getStckPrpr());

                // 수량 계산. (0주 허용)
                long quantityToBuy = shortageAmount / stockPrice;

                // 수량이 0 초과인 경우에만 매수 리스트에 추가.
                if (quantityToBuy > 0) {
                    resultList.add(StockDto.builder()
                            .pdno(stockCode)
                            .ordUnpr(String.valueOf(stockPrice))
                            .ordQty(String.valueOf(quantityToBuy))
                            .orderType(2)
                            .ordDvsn("00")
                            .build()
                    );

                    long buyCost = quantityToBuy * stockPrice;
                    remainCash -= buyCost;

                    log.info("보유 종목 비율 조정 매수: {} ({}주, {}원), 남은 예수금: {}원",
                            stockCode, quantityToBuy, buyCost, remainCash);
                }
            }
        }
        return resultList;
    }

    private void orderStocks(List<StockDto> orders) throws Exception {
        for (StockDto order : orders) {
            if (isValidOrder(order)) {
                domesticStockService.orderDomesticStockCash(order);
            } else {
                log.warn("Invalid order skipped: {}", order);
            }
        }
    }

    private boolean isValidOrder(StockDto order) {
        if (isBlank(order.getPdno())) {
            log.error("주문 실패: 종목코드 누락");
            return false;
        }
        if (!isPositiveNumeric(order.getOrdQty())) {
            log.error("주문 실패: 잘못된 수량 값 -> {}", order.getOrdQty());
            return false;
        }
        if (!isPositiveNumeric(order.getOrdUnpr())) {
            log.error("주문 실패: 잘못된 주문단가 값 -> {}", order.getOrdUnpr());
            return false;
        }
        if (order.getOrderType() <= 0) {
            log.error("주문 실패: 잘못된 주문유형 값 -> {}", order.getOrderType());
            return false;
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isPositiveNumeric(String value) {
        if (isBlank(value)) return false;
        try {
            return Long.parseLong(value) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
