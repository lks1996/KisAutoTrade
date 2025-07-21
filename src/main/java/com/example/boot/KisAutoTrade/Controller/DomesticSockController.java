package com.example.boot.KisAutoTrade.Controller;

import com.example.boot.KisAutoTrade.Service.DomesticStockService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/domesticStock")
public class DomesticSockController {

    private final DomesticStockService domesticStockService;

    @Autowired
    public DomesticSockController(DomesticStockService domesticStockService) {
        this.domesticStockService = domesticStockService;
    }

    /**
     * 국내주식 잔고 조회
     * @throws JsonProcessingException
     */
    @GetMapping("/balance")
    public void requestAccessToken() throws JsonProcessingException {
        domesticStockService.getBalance();
    }
}
