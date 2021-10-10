package com.example.demo.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebScraperServiceImpl implements WebScraperService{
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Override
    @Cacheable(cacheNames = "get-prices", unless = "#result==null")
    public List<Integer> getPrices(String url) throws IOException, ParseException {
        Document doc = Jsoup.parse(new URL("https://www.vrbo.com"+url), 20000);

        return searchLinkTags(doc);
    }

    private List<Integer> searchLinkTags(Document doc) throws ParseException {
        Elements elems = doc.getElementsByTag("script");

        String result[] = new String[2];

        elems.stream().forEach(element -> {
           // LOGGER.info("HEY HELLO     "+element.toString().indexOf("window.__INITIAL_STATE__ = {\"router\":{\"location\":{\"pathname\":"));
            if(element.toString().contains("window.__INITIAL_STATE__ = {\"router\":{\"location\":{\"pathname\":")){
                result[0] = element.toString();
            }
        });


        String resultArray[] = result[0].split("window.__INITIAL_STATE__ = ");

        String resultSet[] = resultArray[1].split("window.__REQUEST_STATE__ = ");

        resultSet[0] = resultSet[0].replaceAll("\\s+","");

        StringBuilder finalResultSet = new StringBuilder(resultSet[0].substring(0,resultSet[0].length()-1));

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(finalResultSet.toString());
        JSONObject jsonObject = (JSONObject) json.get("listingReducer");

        JSONObject ratesJSON = (JSONObject) jsonObject.get("rateSummary");

        ArrayList<Integer> arrayList = (ArrayList<Integer>) ratesJSON.get("rentNights");

        return arrayList;
    }
}
