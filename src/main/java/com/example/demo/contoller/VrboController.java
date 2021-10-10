package com.example.demo.contoller;

import com.example.demo.model.NightlyPrices;
import com.example.demo.model.PropertyInfo;
import com.example.demo.service.VrboService;
import com.example.demo.service.WebScraperService;
import com.example.demo.util.DownloadCSVFile;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class VrboController {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private VrboService vrboService;
    private WebScraperService webScraperService;

    VrboController(VrboService vrboService, WebScraperService webScraperService){
        this.vrboService = vrboService;
        this.webScraperService = webScraperService;
    }

    @GetMapping(path = "/vrbo/getClosestListings/{place}/{radius}")
    public void getClosestListings(@PathVariable String place, @PathVariable int radius, HttpServletResponse response) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("Started fetching records.................................");
        List<PropertyInfo> propertyInfoList = vrboService.getClosestPlaces(place, radius);

        response.setContentType("text/csv");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=closest-places.csv";
        response.setHeader(headerKey, headerValue);

        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
        String[] csvHeader = {"propertyId", "propertyName", "propertyType", "pricePerNight"};
        String[] nameMapping = {"propertyId", "propertyName", "propertyType", "pricePerNight"};

        csvWriter.writeHeader(csvHeader);

        for (PropertyInfo propertyInfo : propertyInfoList) {
            csvWriter.write(propertyInfo, nameMapping);
        }

        csvWriter.close();
        stopWatch.stop();
        LOGGER.info("Task to fetch the records is completed in {} milliseconds",stopWatch.getTotalTimeMillis());

    }

    @GetMapping(path = "/vrbo/topThreePrices/{place}/{radius}")
    public void getTopThreePrices(@PathVariable String place, @PathVariable int radius, HttpServletResponse response) throws IOException{
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("Started fetching records to get three dates with highest price................................");
        List<NightlyPrices> nightlyPricesList = getPrices(place, radius);
        List<List<String>> datesInfo = new ArrayList<>();
        Map<String, List<String>> topPrices = new HashMap<>();
        nightlyPricesList.stream().forEach(nightlyPrices -> {
            datesInfo.add(fetchTopThreePrices(nightlyPrices));
            datesInfo.get(datesInfo.size()-1).add(nightlyPrices.getPropertyInfo().getPropertyName());
        });
        response.setHeader("Content-Disposition", "attachment;filename=nightly-prices.csv");
        response.setHeader("Content-Type", "text/csv");

        OutputStream outputStream = response.getOutputStream();

        DownloadCSVFile.downloadCSVFileForThreeDates(datesInfo, outputStream);

        try{
            outputStream.flush();
            outputStream.close();
            response.flushBuffer();
        }catch (IOException e){
            throw new RuntimeException("Unable to write the data to excel "+e.getMessage(),e);
        }
        stopWatch.stop();
        LOGGER.info("Task to fetch the records to get three dates with highest price, is completed in {} milliseconds",stopWatch.getTotalTimeMillis());
    }

    @GetMapping(path = "/vrbo/nightPrices/{place}/{radius}")
    public void getClosestListingsAndNightlyPrices(@PathVariable String place, @PathVariable int radius, HttpServletResponse response) throws IOException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("Started fetching record prizes for next 12 months................................");

        List<NightlyPrices> nightlyPricesList = getPrices(place, radius);

        LOGGER.info("Listing closest properties and their yearly night prices for next 12 months, based on user's address");
        response.setHeader("Content-Disposition", "attachment;filename=nightly-prices.csv");
        response.setHeader("Content-Type", "text/csv");

        OutputStream outputStream = response.getOutputStream();

        DownloadCSVFile.downloadCSVFile(nightlyPricesList, outputStream);

        try{
            outputStream.flush();
            outputStream.close();
            response.flushBuffer();
        }catch (IOException e){
            throw new RuntimeException("Unable to write the data to excel "+e.getMessage(),e);
        }
        stopWatch.stop();
        LOGGER.info("Task to fetch the record prizes for next 12 months is completed in {} milliseconds",stopWatch.getTotalTimeMillis());
    }

    private List<NightlyPrices> getPrices(String place, int radius){
        List<PropertyInfo> propertyInfoList = vrboService.getClosestPlaces(place, radius);
        List<NightlyPrices> nightlyPricesList = new ArrayList<>();

        propertyInfoList.stream().forEach(propertyInfo -> {
            NightlyPrices nightlyPrices = new NightlyPrices();
            try {
                List<Integer> prices = webScraperService.getPrices(propertyInfo.getDetailedPageUrl());
                nightlyPrices.setPrices(prices);
                nightlyPrices.setPropertyInfo(propertyInfo);
                nightlyPricesList.add(nightlyPrices);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
        return nightlyPricesList;
    }

    private List<String> fetchTopThreePrices(NightlyPrices nightlyPrices){
        int flag = 0;
        int indices[] = new int[3];
        String maxPrice;
        if(nightlyPrices.getPrices() == null){
            flag=1;
            maxPrice = String.valueOf(nightlyPrices.getPropertyInfo().getPricePerNight());
        }else{
            maxPrice = String.valueOf(Collections.max(nightlyPrices.getPrices()));
            int counter =0;
            for(int i=0;i<Math.min(365,nightlyPrices.getPrices().size()); i++){
                if(Integer.valueOf(String.valueOf(nightlyPrices.getPrices().get(i))) == Integer.valueOf((maxPrice))){
                    indices[counter] = i;
                    counter++;
                }
                if(counter == 3)
                    break;
            }

        }
        List<String> topThreePricesWithDates = new ArrayList<>();
        topThreePricesWithDates.add(maxPrice);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();

        if(flag == 0){
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, indices[0]);
            topThreePricesWithDates.add(formatter.format(calendar.getTime()));
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, indices[1]);
            topThreePricesWithDates.add(formatter.format(calendar.getTime()));
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, indices[2]);
            topThreePricesWithDates.add(formatter.format(calendar.getTime()));
        }else{
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 0);
            topThreePricesWithDates.add(formatter.format(calendar.getTime()));
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 1);
            topThreePricesWithDates.add(formatter.format(calendar.getTime()));
            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 2);
            topThreePricesWithDates.add(formatter.format(calendar.getTime()));
        }
        return topThreePricesWithDates;
    }

}
