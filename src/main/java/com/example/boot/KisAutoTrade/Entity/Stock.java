package com.example.boot.KisAutoTrade.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Stock {
    // 날짜, 상한가, 하한가, 누적거래량

    @Id
    @Column(name = "stock_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stockCode;
    private LocalDate date;
    private String maxPrice;
    private String minPrice;
    private String accumTrans;
    private String openPrice;
    private String closePrice;
}
