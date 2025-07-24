package com.example.boot.KisAutoTrade.Controller;

import com.example.boot.KisAutoTrade.Service.SheetDataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sheet")
public class SheetDataImportController {

    private final SheetDataImportService sheetDataImportService;

    @Autowired
    public SheetDataImportController(SheetDataImportService sheetDataImportService) {
        this.sheetDataImportService = sheetDataImportService;
    }

    /**
     * 국내주식 잔고 조회
     */
    @GetMapping("/dataImport")
    public void getDomesticStockBalance() throws Exception{
        sheetDataImportService.getSheetsValue();
    }
}
