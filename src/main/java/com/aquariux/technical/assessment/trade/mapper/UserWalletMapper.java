package com.aquariux.technical.assessment.trade.mapper;

import com.aquariux.technical.assessment.trade.dto.internal.UserWalletDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserWalletMapper {

    @Select("""
            SELECT s.symbol, s.name, uw.balance 
            FROM symbols s 
            INNER JOIN user_wallets uw ON s.id = uw.symbol_id AND uw.user_id = #{userId} 
            ORDER BY s.symbol
            """)
    List<UserWalletDto> findByUserId(Long userId);

    @Update("""
            UPDATE user_wallets
            SET balance = balance + #{amount}
            WHERE user_id = #{userId} AND symbol_id = (SELECT id FROM symbols WHERE symbol = #{symbol})
            """)
    void updateBalance(@Param("userId") Long userId, @Param("symbol") String symbol, @Param("amount")BigDecimal amount);
}