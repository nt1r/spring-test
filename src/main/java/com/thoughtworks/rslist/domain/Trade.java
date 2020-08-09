package com.thoughtworks.rslist.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Trade {
    @Min(value = 1)
    private Integer amount;

    @Min(value = 1)
    private Integer rank;
}
