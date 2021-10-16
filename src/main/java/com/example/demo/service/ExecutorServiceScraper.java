package com.example.demo.service;

import com.example.demo.model.NightlyPrices;
import com.example.demo.model.PropertyInfo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class ExecutorServiceScraper {

    List<NightlyPrices> nightlyPricesList;
    private WebScraperService webScraperService;

    ExecutorServiceScraper(WebScraperService webScraperService) {
        this.webScraperService = webScraperService;
    }

    public List<NightlyPrices> scrape(List<PropertyInfo> propertyInfos) {
        ArrayList<String> arrayList = new ArrayList<>();
        return scrapeUrls(propertyInfos);
    }

    private List<NightlyPrices> scrapeUrls(List<PropertyInfo> propertyInfos) {
        ExecutorService executorService = Executors.newFixedThreadPool(15);
        Map<Future, PropertyInfo> tasks = new LinkedHashMap<>();
        propertyInfos.stream().forEach(propertyInfo -> {
            Callable callable = new Callable<List<Long>>() {
                public List<Long> call() throws Exception {
                    return webScraperService.getPrices(propertyInfo.getDetailedPageUrl());
                }
            };
            Future future = executorService.submit(callable);
            tasks.put(future, propertyInfo);
        });
        nightlyPricesList = new ArrayList<>();
        tasks.forEach((future, propertyInfo) -> {
            try {
                NightlyPrices nightlyPrices = new NightlyPrices();
                nightlyPrices.setPropertyInfo(propertyInfo);
                List<Long> prices = (List<Long>) future.get(120, TimeUnit.SECONDS);
                nightlyPrices.setPrices(prices);
                nightlyPricesList.add(nightlyPrices);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        });
        executorService.shutdown();
        return nightlyPricesList;
    }

}
