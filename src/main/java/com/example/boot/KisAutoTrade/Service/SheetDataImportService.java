package com.example.boot.KisAutoTrade.Service;

import com.example.boot.KisAutoTrade.DTO.SheetDto;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SheetDataImportService {

    @Value("${googleSheetsapi.credentialsFilePath}")
    private String CREDENTIALS_FILE_PATH;
    @Value("${googleSheetsapi.spreadsheetId}")
    private String SHEET_ID;
    @Value("${googleSheetsapi.spreadsheetrange}")
    private String SHEET_RANGE;

    public Sheets getSheets() throws Exception {
        File credentialsFile = new File(CREDENTIALS_FILE_PATH);

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsFile))
                .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Sheets.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                requestInitializer
        ).setApplicationName("Portfolio Rebalancer")
                .build();
    }

    public void getSheetsValue() throws Exception {
        String spreadsheetId = SHEET_ID;
        String range = SHEET_RANGE; // A열: 종목코드, B열: 목표 비율

        Sheets service = getSheets();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        List<List<Object>> rows = response.getValues();
        List<SheetDto> sheetList = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) { // i=1부터 시작: 첫 줄은 헤더
            List<Object> row = rows.get(i);

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

        for (SheetDto row : sheetList) {



            String accType = row.getAccountType();
            String stockType1 = row.getStockType1();
            String stockType2 = row.getStockType2();
            String stockCode = row.getStockCode();
            String stockName = row.getStockName();
            System.out.println(accType + " => " + stockType1 + " => " + stockType2 + " => " + stockCode + " => " + stockName);
        }
    }

}
