package com.example.boot.KisAutoTrade.DTO;

import lombok.Data;

@Data
public class SheetDto {

    // 계좌유형
    String accountType;
    // 구분1
    String stockType1;
    // 구분2
    String stockType2;
    // 종목코드
    String stockCode;
    // 종목명
    String stockName;
    // 구분1 희망 비율
    double categoryTargetRatio;
    // 종목별 희망 비율
    double targetRatio;

    public SheetDto(
            String accountType
            , String stockType1
            , String stockType2
            , String stockCode
            , String stockName
            , double categoryTargetRatio
            , double targetRatio
    ) {
        this.accountType = accountType;
        this.stockType1 = stockType1;
        this.stockType2 = stockType2;
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.categoryTargetRatio = categoryTargetRatio;
        this.targetRatio = targetRatio;
    }

}
