package com.thoughtworks.rslist.util;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;

public class Convertor {
    private Convertor() {}

    public static TradeDto convertTrade2TradeDto(Trade trade, RsEventDto rsEventDto) {
        return TradeDto.builder()
                .amount(trade.getAmount())
                .rank(trade.getRank())
                .rsEvent(rsEventDto)
                .build();
    }
}
