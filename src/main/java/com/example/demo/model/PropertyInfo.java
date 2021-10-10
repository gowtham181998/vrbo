package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PropertyInfo {

    String propertyName;

    double pricePerNight;

    String propertyId;

    String unitId;

    String detailedPageUrl;

    String propertyType;

    Double geoLatitude;

    Double geoLongitude;

    Double averageRating;

    int reviewCount;

}
