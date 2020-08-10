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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
                rsEventRepository.delete(tradeDto.getRsEvent());

                rsEventDto.setBought(true);
                rsEventDto.setRank(trade.getRank());
                rsEventDto = rsEventRepository.save(rsEventDto);

                tradeRepository.save(convertTrade2TradeDto(trade, rsEventDto));
            } else {
                throw new RequestNotValidException("buy amount not enough");
            }
        } else {
            rsEventDto.setBought(true);
            rsEventDto.setRank(trade.getRank());
            rsEventRepository.save(rsEventDto);

            tradeRepository.save(convertTrade2TradeDto(trade, rsEventDto));
        }
    }

    private boolean isBoughtAmountEnough(TradeDto tradeDto, Trade trade) {
        return trade.getAmount() > tradeDto.getAmount();
    }

    private boolean isRankBought(Integer rank) {
        return tradeRepository.findByRank(rank).isPresent();
    }

    public List<RsEventDto> sortRsEvents(List<RsEventDto> rsEventDtoList) {
        List<RsEventDto> totalList = new ArrayList<>(rsEventDtoList);
        List<RsEventDto> boughtList = new ArrayList<>();
        List<RsEventDto> notBoughtList = new ArrayList<>();

        for (RsEventDto rsEventDto: rsEventDtoList) {
            if (rsEventDto.isBought()) {
                boughtList.add(rsEventDto);
            } else {
                notBoughtList.add(rsEventDto);
            }
        }

        notBoughtList.sort(new Comparator<RsEventDto>() {
            @Override
            public int compare(RsEventDto event1, RsEventDto event2) {
                return event2.getVoteNum() - event1.getVoteNum();
            }
        });

        List<Integer> boughtRankList = new ArrayList<>();
        for (RsEventDto boughtEvent: boughtList) {
            int rank = boughtEvent.getRank();
            totalList.set(rank - 1, boughtEvent);
            boughtRankList.add(rank - 1);
        }

        int indexOfNotBoughtList = 0;
        for (int i = 0; i < totalList.size(); ++i) {
            if (!boughtRankList.contains(i)) {
                totalList.set(i, notBoughtList.get(indexOfNotBoughtList++));
            }
        }
        return totalList;
    }
}
