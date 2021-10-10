package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class NightlyPrices {
    private PropertyInfo propertyInfo;

    private List<Integer> prices;
}
