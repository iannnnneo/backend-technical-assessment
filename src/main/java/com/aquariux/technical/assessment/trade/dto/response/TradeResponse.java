package com.aquariux.technical.assessment.trade.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class TradeResponse {
    // TODO: What should you return after a trade is executed?
    private Long tradeId;
    private Long userId;
    private Long cryptoPairId;

    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal totalAmount;

    private Map<String, BigDecimal> updatedWallets;

    private LocalDateTime tradeTime; // timeOfPurchase
}