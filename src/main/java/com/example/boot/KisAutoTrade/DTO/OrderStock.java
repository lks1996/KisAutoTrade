package com.example.boot.KisAutoTrade.DTO;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderStock {

    // 주문타입
    int orderType;
    // 종목코드(6자리)
    String pdno;
    // 주문수량
    String ordQty;
    // 주문단가
    String ordUnpr;
}
