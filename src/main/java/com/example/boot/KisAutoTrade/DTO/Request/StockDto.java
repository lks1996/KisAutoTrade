package com.example.boot.KisAutoTrade.DTO.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDto {

    // 종합계좌번호
    String cano;
    // 계좌상품코드
    String acntPrdtCd;
    // 시간외단일가, 거래소여부
    String afhrFlprYn = "N";
    // 오프라인여부
    String oflYn = "";
    // 조회구분
    String inorDvsn = "01";
    // 단가구분
    String unprDvsn = "01";
    // 펀드결제분포함여부
    String fundSttlIcldYn = "Y";
    // 융자금액자동상환여부
    String fncgAmtAutoRdptYn = "N";
    // 처리구분
    String prcsDvsn = "00";
    // 연속조회검색조건 100
    String ctxAreaFk100 = "";
    // 연속조회키 100
    String ctxAreaNk100 = "";


    // 조건 시장 분류 코드( J:KRX, NX:NXT, UN:통합 )
    String fidCondMrktDivCode = "UN";
    // 입력 종목코드 (ex 005930 삼성전자)
    String fidInputIscd = "";


    // 주문타입
    int orderType;
    // 종목코드(6자리)
    String pdno;
    // 주문구분 ( 00 : KRX NXT SOR 지정가 , 01 : KRX SOR 시장가)
    String ordDvsn = "00";
    // 주문수량
    String ordQty;
    // 주문단가
    String ordUnpr;

}
