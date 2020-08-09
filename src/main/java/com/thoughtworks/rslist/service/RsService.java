package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.exception.RequestNotValidException;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.thoughtworks.rslist.util.Convertor.convertTrade2TradeDto;

@Service
public class RsService {
    final RsEventRepository rsEventRepository;
    final UserRepository userRepository;
    final VoteRepository voteRepository;
    final TradeRepository tradeRepository;

    public RsService(RsEventRepository rsEventRepository,
                     UserRepository userRepository,
                     VoteRepository voteRepository,
                     TradeRepository tradeRepository) {
        this.rsEventRepository = rsEventRepository;
        this.userRepository = userRepository;
        this.voteRepository = voteRepository;
        this.tradeRepository = tradeRepository;
    }

    public void vote(Vote vote, int rsEventId) {
        Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
        Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
        if (!rsEventDto.isPresent()
                || !userDto.isPresent()
                || vote.getVoteNum() > userDto.get().getVoteNum()) {
            throw new RuntimeException();
        }
        VoteDto voteDto =
                VoteDto.builder()
                        .localDateTime(vote.getTime())
                        .num(vote.getVoteNum())
                        .rsEvent(rsEventDto.get())
                        .user(userDto.get())
                        .build();
        voteRepository.save(voteDto);
        UserDto user = userDto.get();
        user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
        userRepository.save(user);
        RsEventDto rsEvent = rsEventDto.get();
        rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
        rsEventRepository.save(rsEvent);
    }

    public void buy(Trade trade, int id) {
        RsEventDto rsEventDto = rsEventRepository.findById(id).get();
        if (isRankBought(trade.getRank())) {
            TradeDto tradeDto = tradeRepository.findByRank(trade.getRank()).get();
            if (isBoughtAmountEnough(tradeDto, trade)) {
                rsEventRepository.delete(rsEventDto);
                tradeDto.setAmount(trade.getAmount());
                tradeRepository.save(tradeDto);
            } else {
                throw new RequestNotValidException("buy amount not enough");
            }
        } else {
            tradeRepository.save(convertTrade2TradeDto(trade, rsEventDto));
        }
    }

    private boolean isBoughtAmountEnough(TradeDto tradeDto, Trade trade) {
        return trade.getAmount() > tradeDto.getAmount();
    }

    private boolean isRankBought(Integer rank) {
        return tradeRepository.findByRank(rank).isPresent();
    }
}
