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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.thoughtworks.rslist.util.Convertor.convertTrade2TradeDto;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class RsServiceTest {
    RsService rsService;

    @Mock
    RsEventRepository rsEventRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    VoteRepository voteRepository;
    @Mock
    TradeRepository tradeRepository;

    LocalDateTime localDateTime;
    Vote vote;

    @BeforeEach
    void setUp() {
        initMocks(this);
        rsService = new RsService(rsEventRepository, userRepository, voteRepository, tradeRepository);
        localDateTime = LocalDateTime.now();
        vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
    }

    @Test
    void shouldVoteSuccess() {
        // given

        UserDto userDto =
                UserDto.builder()
                        .voteNum(5)
                        .phone("18888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("xiaoli")
                        .id(2)
                        .build();
        RsEventDto rsEventDto =
                RsEventDto.builder()
                        .eventName("event name")
                        .id(1)
                        .keyword("keyword")
                        .voteNum(2)
                        .user(userDto)
                        .build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
        // when
        rsService.vote(vote, 1);
        // then
        verify(voteRepository)
                .save(
                        VoteDto.builder()
                                .num(2)
                                .localDateTime(localDateTime)
                                .user(userDto)
                                .rsEvent(rsEventDto)
                                .build());
        verify(userRepository).save(userDto);
        verify(rsEventRepository).save(rsEventDto);
    }

    @Test
    void shouldThrowExceptionWhenUserNotExist() {
        // given
        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
        //when&then
        assertThrows(
                RuntimeException.class,
                () -> {
                    rsService.vote(vote, 1);
                });
    }

    @Test
    void shouldBuySuccessIfRsEventHasNotBeenBought() {
        // Given
        UserDto userDto = UserDto.builder().voteNum(10).phone("18888888888").gender("female").email("a@b.com").age(19).userName("xiaoli").build();
        RsEventDto rsEventDto = RsEventDto.builder().eventName("event_1").keyword("class_1").voteNum(0).isBought(false).id(100).user(userDto).build();
        Trade trade = Trade.builder().amount(100).rank(1).build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
        when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.empty());

        // When
        rsService.buy(trade, rsEventDto.getId());

        // Then
        verify(rsEventRepository).save(rsEventDto);
        verify(tradeRepository).save(TradeDto.builder().amount(trade.getAmount()).rank(trade.getRank()).rsEvent(rsEventDto).build());
    }

    @Test
    void shouldBuySuccessIfPaidMoreThanPrevious() {
        // Given
        UserDto userDto = UserDto.builder().voteNum(10).phone("18888888888").gender("female").email("a@b.com").age(19).userName("xiaoli").build();
        RsEventDto rsEventDto1 = RsEventDto.builder().eventName("event_1").keyword("class_1").voteNum(0).isBought(false).rank(1).id(100).user(userDto).build();
        RsEventDto rsEventDto2 = RsEventDto.builder().eventName("event_2").keyword("class_2").voteNum(0).isBought(false).rank(1).id(200).user(userDto).build();
        Trade trade100 = Trade.builder().amount(100).rank(1).build();
        Trade trade200 = Trade.builder().amount(200).rank(1).build();


        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto2));
        when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(convertTrade2TradeDto(trade100, rsEventDto1)));

        // When
        rsService.buy(trade200, rsEventDto2.getId());

        // Then
        verify(rsEventRepository).save(rsEventDto2);
        verify(tradeRepository, times(1)).save(any());
    }

    @Test
    void shouldBuyFailIfPaidLessThanPrevious() {
        // Given
        UserDto userDto = UserDto.builder().voteNum(10).phone("18888888888").gender("female").email("a@b.com").age(19).userName("xiaoli").build();
        RsEventDto rsEventDto1 = RsEventDto.builder().eventName("event_1").keyword("class_1").voteNum(0).isBought(false).id(100).user(userDto).build();
        RsEventDto rsEventDto2 = RsEventDto.builder().eventName("event_2").keyword("class_2").voteNum(0).isBought(false).id(200).user(userDto).build();
        Trade trade100 = Trade.builder().amount(100).rank(1).build();
        Trade trade80 = Trade.builder().amount(80).rank(1).build();

        when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto2));
        when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(convertTrade2TradeDto(trade100, rsEventDto1)));

        // Then
        assertThrows(RequestNotValidException.class, () -> {
            // When
            rsService.buy(trade80, rsEventDto2.getId());
        });
        verify(tradeRepository, times(0)).save(any());
    }
}
