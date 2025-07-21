package com.example.boot.KisAutoTrade.Repository;

import com.example.boot.KisAutoTrade.Entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {}

