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

        for (int i = 1; i < sheetDataList.size(); i++) { // i=1부터 시작: 첫 줄은 헤더
            List<Object> row = sheetDataList.get(i);

//            String accountType = row.size() > 0 ? row.get(0).toString() : "";
//            String stockType1 = row.size() > 1 ? row.get(1).toString() : "";
            double categoryTargetRatio = row.size() > 0 ? Double.parseDouble(row.get(0).toString()) : 0;
            String stockType2 = row.size() > 1 ? row.get(1).toString() : "";
            String stockCode = row.size() > 2 ? row.get(2).toString() : "";
            String stockName = row.size() > 3 ? row.get(3).toString() : "";
            double targetRatio = row.size() > 4 ? Double.parseDouble(row.get(4).toString()) : 0;

            SheetDto dto = new SheetDto("", "", stockType2, stockName,  stockCode, categoryTargetRatio, targetRatio);
            sheetList.add(dto);
        }

        // 2. 잔고 조회
        StockDto requestDto = new StockDto();
        String balancerResponse = domesticStockService.getBalance(requestDto);

        ObjectMapper mapper = new ObjectMapper();
        StockBalanceResponseDto sbrDto = mapper.readValue(balancerResponse, StockBalanceResponseDto.class);


        List<BalanceOutput1Dto> holdingStocks = sbrDto.getOutput1();

        log.info("예수금 총액: {}", sbrDto.getOutput2().get(0).getDncaTotAmt());

        /** 1. 보유하고 있지 않은 종목이 있다면, 해당 종목을 먼저 구매함. */

        List<SheetDto> unholdingStockList = sheetList.stream()
                .filter(sheet -> sbrDto.getOutput1().stream()
                        .noneMatch(own -> own.getPdno().equals(sheet.getStockCode())))
                .toList();

        if (!unholdingStockList.isEmpty()) {

            Map<String, String> holdingMap = holdingStocks.stream()
                    .collect(Collectors.toMap(BalanceOutput1Dto::getPdno, BalanceOutput1Dto::getEvluAmt));

            // 현재 총 보유금액
            long totalHoldingAmount = holdingStocks.stream()
                    .mapToLong(h -> Long.parseLong(h.getEvluAmt()))
                    .sum();

            // 총 보유금액 = X * Σ(targetRatio of holding 종목)
            double sumRatioOfHoldings = sheetList.stream()
                    .filter(p -> holdingMap.containsKey(p.getStockCode()))
                    .mapToDouble(SheetDto::getTargetRatio)
                    .sum();

            double estimatedTotalPortfolio = totalHoldingAmount / (sumRatioOfHoldings / 100.0);

            List<StockDto> toBuyList =  new ArrayList<>();
            // 미보유 종목별 구매 필요 금액 계산
            for (SheetDto p : sheetList) {
                if (!holdingMap.containsKey(p.getStockCode())) {
                    long needToBuyAmount = Math.round(estimatedTotalPortfolio * (p.getTargetRatio() / 100.0));

                    // 현재가 조회
                    StockDto stockPriceDto = new StockDto();
                    stockPriceDto.setFidInputIscd(p.getStockCode());

                    JsonNode rootNode = mapper.readTree(domesticStockService.getDomesticStockPrice(stockPriceDto));
                    JsonNode outputNode = rootNode.path("output");
                    StockPriceResponseDto sprDto = mapper.treeToValue(outputNode, StockPriceResponseDto.class);

                    long stockPrice = Long.parseLong(sprDto.getStckPrpr());
                    long quantityToBuy = stockPrice > 0 ? needToBuyAmount / stockPrice : 0;

                    // toBuyList에 추가 (빌더 패턴 사용)
                    toBuyList.add(StockDto.builder()
                            .pdno(p.getStockCode())
                            .ordUnpr(String.valueOf(stockPrice))
                            .ordQty(String.valueOf(quantityToBuy))
                            .build()
                    );
                    log.info("미보유 종목코드: {}", p.getStockCode());
                    log.info("미보유 종목 주문 예정가: {}", stockPrice);
                    log.info("미보유 종목 주문 예정 수량: {}", quantityToBuy);
                    log.info("========================================");
                }
            }
            log.info("===미보유 종목 존재===");
        }

        /** 2. 이후에 보유 종목에 대해 포트폴리오 비율과 비교하여 추가 매수 진행.(보유 종목의 현재 비율은 내림 처리.) */



        // 3. 현재가 조회

        StockDto stockPriceDto = new StockDto();
        domesticStockService.getDomesticStockPrice(stockPriceDto);

        // 4. 주문 실행
        StockDto stockOrderDto = new StockDto();
        domesticStockService.orderDomesticStockCash(stockOrderDto);
    }
}
