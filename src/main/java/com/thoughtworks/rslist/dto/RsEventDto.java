package com.thoughtworks.rslist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventDto {
    @Id
    @GeneratedValue
    private int id;
    private String eventName;
    private String keyword;
    private int voteNum;
    private int rank;
    @Builder.Default
    private boolean isBought = false;
    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "rsEvent")
    private List<TradeDto> trades;
    @ManyToOne
    private UserDto user;
}
