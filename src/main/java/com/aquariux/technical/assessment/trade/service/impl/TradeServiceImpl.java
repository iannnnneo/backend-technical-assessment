package com.aquariux.technical.assessment.trade.service.impl;

import com.aquariux.technical.assessment.trade.dto.request.TradeRequest;
import com.aquariux.technical.assessment.trade.dto.response.BestPriceResponse;
import com.aquariux.technical.assessment.trade.dto.response.TradeResponse;
import com.aquariux.technical.assessment.trade.dto.response.WalletBalanceResponse;
import com.aquariux.technical.assessment.trade.entity.Trade;
import com.aquariux.technical.assessment.trade.enums.TradeType;
import com.aquariux.technical.assessment.trade.exception.InsufficientBalanceException;
import com.aquariux.technical.assessment.trade.mapper.CryptoPairMapper;
import com.aquariux.technical.assessment.trade.mapper.TradeMapper;
import com.aquariux.technical.assessment.trade.mapper.UserWalletMapper;
import com.aquariux.technical.assessment.trade.service.PriceServiceInterface;
import com.aquariux.technical.assessment.trade.service.TradeServiceInterface;
import com.aquariux.technical.assessment.trade.service.WalletServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeServiceInterface {

    private final TradeMapper tradeMapper;
    // Add additional beans here if needed for your implementation
    private final WalletServiceInterface walletServiceInterface;
    private final PriceServiceInterface priceServiceInterface;
    private final CryptoPairMapper cryptoPairMapper;
    private final UserWalletMapper userWalletMapper;

    @Override
    public TradeResponse executeTrade(TradeRequest tradeRequest) {
        // TODO: Implement the core trading engine
        // What should happen when a user executes a trade?
        TradeType tradeType = tradeRequest.getTradeType();
        long cryptoPairId = tradeRequest.getCryptoPairId();

        // get the wallet balance
        List <WalletBalanceResponse> walletBalanceResponses = walletServiceInterface.getUserWalletBalances(tradeRequest.getUserId());
        Map<String, WalletBalanceResponse> walletBalanceResponseMap = walletBalanceResponses.stream().collect(Collectors.toMap(WalletBalanceResponse::getSymbol, w -> w));

        // get the latest price of the crypto that user want to buy
        List <BestPriceResponse> bestPriceResponsesList = priceServiceInterface.getLatestBestPrices();
        Map <String, BestPriceResponse> bestPriceResponseMap = bestPriceResponsesList.stream().collect(Collectors.toMap(BestPriceResponse::getPairName, b -> b));
        Map<Long, String> idToPairName = bestPriceResponseMap.keySet().stream()
                .collect(Collectors.toMap(
                        cryptoPairMapper::findIdByPairName, // id
                        pairName -> pairName // value
                ));

        String pairName = idToPairName.get(cryptoPairId);
        if (pairName == null) {
            throw new RuntimeException("Cannot find pairName for cryptoPairId " + cryptoPairId);
        }
        String baseSymbol = pairName.substring(0, pairName.length() - 4);
        String quoteSymbol = pairName.substring(pairName.length() - 4);
        BestPriceResponse priceResponse = bestPriceResponseMap.get(pairName);
        if (priceResponse == null) {
            throw new RuntimeException("Cannot find market price for pair " + pairName);
        }

        BigDecimal tradePrice = (tradeType == TradeType.BUY) ? priceResponse.getAskPrice() : priceResponse.getBidPrice();
        WalletBalanceResponse baseWallet = walletBalanceResponseMap.get(baseSymbol);
        WalletBalanceResponse quoteWallet = walletBalanceResponseMap.get(quoteSymbol);

        // set the debit and credit wallet according to the trade type
        WalletBalanceResponse debitWallet = (tradeType == TradeType.BUY) ? quoteWallet : baseWallet;
        WalletBalanceResponse creditWallet = (tradeType == TradeType.BUY) ? baseWallet : quoteWallet;

        BigDecimal debitAmount = (tradeType == TradeType.BUY)
                ? tradePrice.multiply(tradeRequest.getQuantity())
                : tradeRequest.getQuantity();

        BigDecimal creditAmount = (tradeType == TradeType.BUY)
                ? tradeRequest.getQuantity()
                : tradePrice.multiply(tradeRequest.getQuantity());

        // check if the wallet balance has enough to purchase crypto pair. throw exception if not enough
        if (debitWallet.getBalance().compareTo(debitAmount) < 0)
            throw new InsufficientBalanceException(debitWallet.getSymbol());

        debitWallet.setBalance(debitWallet.getBalance().subtract(debitAmount));
        creditWallet.setBalance(creditWallet.getBalance().add(creditAmount));

        // create a map to store the updated wallet balance
        Map <String, BigDecimal> updatedWalletBalances = walletBalanceResponseMap.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().getBalance()
        ));

        Trade trade = new Trade();
        trade.setUserId(tradeRequest.getUserId());
        trade.setCryptoPairId(cryptoPairId);
        trade.setTradeType(String.valueOf(tradeType));
        trade.setQuantity(tradeRequest.getQuantity());
        trade.setPrice(tradePrice);
        trade.setTotalAmount(debitAmount);
        trade.setTradeTime(LocalDateTime.now());

        // update the databases
        tradeMapper.trade(trade);
        userWalletMapper.updateBalance(tradeRequest.getUserId(), debitWallet.getSymbol(), debitAmount.negate());
        userWalletMapper.updateBalance(tradeRequest.getUserId(), creditWallet.getSymbol(), creditAmount);

        return mapToResponse(trade, updatedWalletBalances);

        // throw new UnsupportedOperationException("Trade execution logic to be implemented");
    }

    private TradeResponse mapToResponse(Trade trade, Map<String, BigDecimal> updatedWalletBalances) {
        TradeResponse tradeResponse = new TradeResponse();
        tradeResponse.setTradeId(trade.getId());
        tradeResponse.setUserId(trade.getUserId());
        tradeResponse.setCryptoPairId(trade.getCryptoPairId());
        tradeResponse.setPrice(trade.getPrice());
        tradeResponse.setQuantity(trade.getQuantity());
        tradeResponse.setTotalAmount(trade.getTotalAmount());
        tradeResponse.setUpdatedWallets(updatedWalletBalances);
        tradeResponse.setTradeTime(trade.getTradeTime());
        return tradeResponse;
    }
}