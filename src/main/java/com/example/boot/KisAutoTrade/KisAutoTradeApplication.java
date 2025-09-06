package com.example.boot.KisAutoTrade;

import com.example.boot.KisAutoTrade.Service.AutoTradeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KisAutoTradeApplication implements CommandLineRunner {

	private final AutoTradeService autoTradeService;

	public KisAutoTradeApplication(AutoTradeService autoTradeService) {
		this.autoTradeService = autoTradeService;
	}

	public static void main(String[] args) {
		SpringApplication.run(KisAutoTradeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		autoTradeService.execute();
		System.exit(0);
	}
}
