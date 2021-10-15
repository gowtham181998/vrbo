package com.example.demo.response;

import com.example.demo.model.PropertyInfo;
import com.example.demo.util.DistanceCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SampleResponseBuilder {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public List<PropertyInfo> buildResponseForBulk(Map<String, Object> response, int radius){

        List<PropertyInfo> propertyInfos = new ArrayList<>();
        Map<String, Object> data = retrieveInnerData(response, "data");

        Map<String, Object> results = retrieveInnerData(data, "results");

        if(results == null){
            LOGGER.error("AN ERROR OCCURRED WHILE TRYING TO FETCH THE DATA DUE TO {}", response.get("errors"));
            return null;
        }

        Map<String, Object> geography = retrieveInnerData(results, "geography");

        Map<String, Object> location = retrieveInnerData(geography, "location");

        Double currentLatitude = retrieveDoubleValue(location, "latitude");
        Double currentLongitude = retrieveDoubleValue(location, "longitude");

        AtomicInteger counter = new AtomicInteger();
        AtomicInteger numberOfPlacesWithinGivenRadius = new AtomicInteger();
        List<Map<String, Object>> listings = null;
        if(results !=null) {
            listings = (List<Map<String, Object>>) results.get("listings");
        }

        if(listings!=null){
            listings.stream().forEach(listing -> {
                PropertyInfo propertyInfo = new PropertyInfo();
                propertyInfo.setPropertyId(retrieveStringValue(listing, "propertyId"));

                Map<String, Object> prices = retrieveInnerData(listing, "prices");
                Map<String, Object> perNight = retrieveInnerData(prices, "perNight");
                propertyInfo.setPricePerNight(retrieveDoubleValue(perNight, "amount"));

                Map<String, Object> propertyMetaData = retrieveInnerData(listing, "propertyMetadata");
                propertyInfo.setPropertyName(retrieveStringValue(propertyMetaData,"headline"));

                propertyInfo.setPropertyType(retrieveStringValue(listing, "propertyType"));

                propertyInfo.setAverageRating(retrieveDoubleValue(listing, "averageRating"));

                propertyInfo.setReviewCount(retrieveIntValue(listing, "reviewCount"));

                Map<String,Object> geoCode = retrieveInnerData(listing, "geoCode");

                Map<String, Object> unitMetadata = retrieveInnerData(listing, "unitMetadata");

                String unitName = retrieveStringValue(unitMetadata, "unitName");

                if(unitName!=null) {
                    String unitDetails[] = unitName.split("unit_");

                    propertyInfo.setUnitId(unitDetails.length > 1 ? unitDetails[1] : null);
                }

                propertyInfo.setDetailedPageUrl(retrieveStringValue(listing, "detailPageUrl"));

                propertyInfo.setGeoLatitude(retrieveDoubleValue(geoCode, "latitude"));
                propertyInfo.setGeoLongitude(retrieveDoubleValue(geoCode, "longitude"));

                Double distance = DistanceCalculator.distance(currentLatitude, currentLongitude, propertyInfo.getGeoLatitude(), propertyInfo.getGeoLongitude(), 'K');

                // LOGGER.info("distance between {},{} to {},{} is {}",currentLatitude,currentLongitude,propertyInfo.getGeoLatitude(),propertyInfo.getGeoLongitude(),distance);
                counter.addAndGet(1);
                if (currentLatitude == null || currentLongitude == null || propertyInfo.getGeoLatitude() == null || propertyInfo.getGeoLongitude() == null) {
                    propertyInfos.add(propertyInfo);
                    numberOfPlacesWithinGivenRadius.addAndGet(1);
                } else if (distance < radius && currentLatitude != null && currentLongitude != null) {
                    propertyInfos.add(propertyInfo);
                    numberOfPlacesWithinGivenRadius.addAndGet(1);
                }
            });
        }
        LOGGER.info("Total number of records fetched are {} out of which {} are within the given radius", counter, numberOfPlacesWithinGivenRadius);
        return propertyInfos;

    }

    private Map<String, Object> retrieveInnerData(Map<String, Object> response,String key){
        Map<String, Object> innerData = null;
        if(response != null && !StringUtils.isEmpty(key) && response.containsKey(key)){
            innerData = (Map) response.get(key);
        }else{
            LOGGER.error("Unable to retrieve data with key {}"+key);
        }
        return innerData;
    }

    private String retrieveStringValue(Map<String, Object> response,String key){
        String value = null;
        if(response != null && !StringUtils.isEmpty(key) && response.containsKey(key)){
            value = String.valueOf(response.get(key));
        }else{
            LOGGER.error("Unable to retrieve data with key {}"+key);
        }
        return value;
    }

    private int retrieveIntValue(Map<String, Object> response,String key){
        int value = 0;
        if(response != null && !StringUtils.isEmpty(key) && response.containsKey(key)){
            value = (int) response.get(key);
        }else{
            LOGGER.error("Unable to retrieve data with key {}"+key);
        }
        return value;
    }

    private Double retrieveDoubleValue(Map<String, Object> response,String key){
        Double value = 0.0;
        if(response != null && !StringUtils.isEmpty(key) && response.containsKey(key)){
            value = Double.valueOf(String.valueOf(response.get(key)));
        }else{
            LOGGER.error("Unable to retrieve data with key {}"+key);
        }
        return value;
    }

}
