package com.thoughtworks.rslist.api;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RsControllerTest {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RsEventRepository rsEventRepository;
    @Autowired
    VoteRepository voteRepository;
    @Autowired
    TradeRepository tradeRepository;
    @Autowired
    private MockMvc mockMvc;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
        rsEventRepository.deleteAll();
        userRepository.deleteAll();
        userDto =
                UserDto.builder()
                        .voteNum(10)
                        .phone("188888888888")
                        .gender("female")
                        .email("a@b.com")
                        .age(19)
                        .userName("idolice")
                        .build();
    }

    @Test
    public void shouldGetRsEventList() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

        rsEventRepository.save(rsEventDto);

        mockMvc
                .perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[0]", not(hasKey("user"))))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetOneEvent() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
        mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
        mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
        mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
    }

    @Test
    public void shouldGetErrorWhenIndexInvalid() throws Exception {
        mockMvc
                .perform(get("/rs/4"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("invalid index")));
    }

    @Test
    public void shouldGetRsListBetween() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
        rsEventRepository.save(rsEventDto);
        mockMvc
                .perform(get("/rs/list?start=1&end=2"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")));
        mockMvc
                .perform(get("/rs/list?start=2&end=3"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")));
        mockMvc
                .perform(get("/rs/list?start=1&end=3"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].keyword", is("无分类")))
                .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
                .andExpect(jsonPath("$[1].keyword", is("无分类")))
                .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
                .andExpect(jsonPath("$[2].keyword", is("无分类")));
    }

    @Test
    public void shouldAddRsEventWhenUserExist() throws Exception {

        UserDto save = userRepository.save(userDto);

        String jsonValue =
                "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

        mockMvc
                .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
        List<RsEventDto> all = rsEventRepository.findAll();
        assertNotNull(all);
        assertEquals(all.size(), 1);
        assertEquals(all.get(0).getEventName(), "猪肉涨价了");
        assertEquals(all.get(0).getKeyword(), "经济");
        assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
        assertEquals(all.get(0).getUser().getAge(), save.getAge());
    }

    @Test
    public void shouldAddRsEventWhenUserNotExist() throws Exception {
        String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
        mockMvc
                .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldVoteSuccess() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventDto = rsEventRepository.save(rsEventDto);

        String jsonValue =
                String.format(
                        "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
                        save.getId(), LocalDateTime.now().toString());
        mockMvc
                .perform(
                        post("/rs/vote/{id}", rsEventDto.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        UserDto userDto = userRepository.findById(save.getId()).get();
        RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
        assertEquals(userDto.getVoteNum(), 9);
        assertEquals(newRsEvent.getVoteNum(), 1);
        List<VoteDto> voteDtos = voteRepository.findAll();
        assertEquals(voteDtos.size(), 1);
        assertEquals(voteDtos.get(0).getNum(), 1);
    }

    @Test
    public void shouldBuySuccessIfRsEventHasNotBeenBought() throws Exception {
        UserDto save = userRepository.save(userDto);
        RsEventDto rsEventDto =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventDto = rsEventRepository.save(rsEventDto);

        String jsonValue =
                String.format(
                        "{\"amount\":%d,\"rank\":\"%d\"}",
                        100, 1);
        mockMvc
                .perform(
                        post("/rs/buy/{id}", rsEventDto.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, tradeRepository.count());

        TradeDto tradeDto = tradeRepository.findAll().get(0);
        assertEquals(100, tradeDto.getAmount());
        assertEquals(1, tradeDto.getRank());
    }

    @Test
    public void shouldBuySuccessIfPaidMoreThanPrevious() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventDto1 = rsEventRepository.save(rsEventDto1);
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        rsEventDto2 = rsEventRepository.save(rsEventDto2);

        String jsonValue =
                String.format(
                        "{\"amount\":%d,\"rank\":\"%d\"}",
                        100, 1);
        mockMvc
                .perform(
                        post("/rs/buy/{id}", rsEventDto1.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, tradeRepository.count());

        jsonValue =
                String.format(
                        "{\"amount\":%d,\"rank\":\"%d\"}",
                        120, 1);
        mockMvc
                .perform(
                        post("/rs/buy/{id}", rsEventDto2.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, tradeRepository.count());
        assertEquals(1, rsEventRepository.count());

        TradeDto tradeDto = tradeRepository.findAll().get(0);
        assertEquals(120, tradeDto.getAmount());
        assertEquals(1, tradeDto.getRank());
    }

    @Test
    public void shouldBuyFailIfPaidLessThanPrevious() throws Exception {
        UserDto save = userRepository.save(userDto);

        RsEventDto rsEventDto1 =
                RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
        rsEventDto1 = rsEventRepository.save(rsEventDto1);
        RsEventDto rsEventDto2 =
                RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
        rsEventDto2 = rsEventRepository.save(rsEventDto2);

        String jsonValue =
                String.format(
                        "{\"amount\":%d,\"rank\":\"%d\"}",
                        100, 1);
        mockMvc
                .perform(
                        post("/rs/buy/{id}", rsEventDto1.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(1, tradeRepository.count());

        jsonValue =
                String.format(
                        "{\"amount\":%d,\"rank\":\"%d\"}",
                        80, 1);
        mockMvc
                .perform(
                        post("/rs/buy/{id}", rsEventDto2.getId())
                                .content(jsonValue)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertEquals(1, tradeRepository.count());
        assertEquals(2, rsEventRepository.count());

        TradeDto tradeDto = tradeRepository.findAll().get(0);
        assertEquals(100, tradeDto.getAmount());
        assertEquals(1, tradeDto.getRank());
    }

    @Test
    public void shouldSortByVoteNumsWhenGetRsEvents() throws Exception {
        userDto = userRepository.save(userDto);

        RsEventDto rsEventDto1 = RsEventDto.builder().eventName("event_1").keyword("class_1").user(userDto).voteNum(6).build();
        RsEventDto rsEventDto2 = RsEventDto.builder().eventName("event_2").keyword("class_2").user(userDto).voteNum(3).build();
        RsEventDto rsEventDto3 = RsEventDto.builder().eventName("event_3").keyword("class_3").user(userDto).voteNum(1).build();
        RsEventDto rsEventDto4 = RsEventDto.builder().eventName("event_4").keyword("class_4").user(userDto).voteNum(0).build();
        RsEventDto rsEventDto5 = RsEventDto.builder().eventName("event_5").keyword("class_5").user(userDto).voteNum(0).build();
        rsEventDto1 = rsEventRepository.save(rsEventDto1);
        rsEventDto2 = rsEventRepository.save(rsEventDto2);
        rsEventDto3 = rsEventRepository.save(rsEventDto3);
        rsEventDto4 = rsEventRepository.save(rsEventDto4);
        rsEventDto5 = rsEventRepository.save(rsEventDto5);

        TradeDto tradeDtoOf4thEvent = TradeDto.builder().amount(200).rank(1).rsEvent(rsEventDto4).build();
        TradeDto tradeDtoOf5thEvent = TradeDto.builder().amount(100).rank(3).rsEvent(rsEventDto5).build();
        tradeRepository.save(tradeDtoOf4thEvent);
        tradeRepository.save(tradeDtoOf5thEvent);

        mockMvc
                .perform(get("/rs/list"))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].eventName", is("event_4")))
                .andExpect(jsonPath("$[1].eventName", is("event_1")))
                .andExpect(jsonPath("$[2].eventName", is("event_5")))
                .andExpect(jsonPath("$[3].eventName", is("event_2")))
                .andExpect(jsonPath("$[4].eventName", is("event_3")))
                .andExpect(status().isOk());
    }
}
