package com.SafeCryptoStocks.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SafeCryptoStocks.model.Portfolio;
import com.SafeCryptoStocks.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByPortfolio(Portfolio portfolio);




}

