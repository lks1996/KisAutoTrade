package com.example.boot.KisAutoTrade.Service;

import com.example.boot.KisAutoTrade.DTO.SheetDto;
import com.example.boot.KisAutoTrade.DTO.StockBalanceResponseDto;
import com.example.boot.KisAutoTrade.DTO.StockDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

            String accountType = row.size() > 0 ? row.get(0).toString() : "";
            String stockType1 = row.size() > 1 ? row.get(1).toString() : "";
            String stockType2 = row.size() > 2 ? row.get(2).toString() : "";
            String stockCode = row.size() > 3 ? row.get(3).toString() : "";
            String stockName = row.size() > 4 ? row.get(4).toString() : "";
            double categoryTargetRatio = row.size() > 5 ? Double.parseDouble(row.get(5).toString()) : 0;
            double targetRatio = row.size() > 6 ? Double.parseDouble(row.get(6).toString()) : 0;

            SheetDto dto = new SheetDto(accountType, stockType1, stockType2, stockCode,  stockName, categoryTargetRatio, targetRatio);
            sheetList.add(dto);
        }

        // 2. 잔고 조회
        StockDto requestDto = new StockDto();
        String balancerResponse = domesticStockService.getBalance(requestDto);

        ObjectMapper mapper = new ObjectMapper();
        StockBalanceResponseDto sbrDto = mapper.readValue(balancerResponse, StockBalanceResponseDto.class);


        log.info("예수금 총액: {}", sbrDto.getOutput2().get(0).getDncaTotAmt());
        log.info("현재 보유 종목 수: {}", sbrDto.getOutput1().size());



//        List<StockBalanceDto> stockBalanceList = response;
//        ObjectMapper mapper = new ObjectMapper();
//        List<StockBalanceDto> input1List = mapper.convertValue(
//                response.get("input1"), new TypeReference<List<StockBalanceDto>>() {}
//        );
        // 매수가 필요한 종목 리스트 생성.
            // 구글시트 데이터 종목에 내 보유 종목이 있는 경우
                // 구글시트 데이터 종목의 비율과 내가 보유하고 있는 종목의 비율이 동일하지 않은 경우(소수점은 내림 처리)
                    // 리스트에 종목 정보 추가.

        // 구글시트 데이터 종목이 내가 보유한 종목이 아닌 경우
            // 리스트에 종목 추가.


        // 3. 현재가 조회

        StockDto stockPriceDto = new StockDto();
        domesticStockService.getDomesticStockPrice(stockPriceDto);

        // 4. 주문 실행
        StockDto stockOrderDto = new StockDto();
        domesticStockService.orderDomesticStockCash(stockOrderDto);
    }
}
