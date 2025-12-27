package com.aquariux.technical.assessment.trade.dto.request;

import com.aquariux.technical.assessment.trade.enums.TradeType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TradeRequest {
    private Long userId;
    private TradeType tradeType;
    
    // TODO: What information do you need to execute a trade?
    private Long cryptoPairId;
    private BigDecimal quantity;
//    private BigDecimal price; // price get from the api latest price
}