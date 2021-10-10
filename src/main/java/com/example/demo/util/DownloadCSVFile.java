package com.example.demo.util;

import com.example.demo.model.NightlyPrices;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class DownloadCSVFile {

    public static void downloadCSVFile(List<NightlyPrices> nightlyPricesList, OutputStream outputStream) throws IOException {

        String headLine = "PropertyID ,PropertyName ,PropertyType ,PricePerNight";

        StringBuilder dates = new StringBuilder();

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        String incrementDate = formatter.format(calendar.getTime());
        for(int i=0; i<365; i++) {
            dates.append(", ").append(incrementDate);
            calendar.add(Calendar.DATE, 1);
            incrementDate = formatter.format(calendar.getTime());
        }

        headLine = headLine + dates + "\n";

        outputStream.write(headLine.getBytes());

        nightlyPricesList.stream().forEach(nightlyPrices -> {
            try {
                StringBuilder record = new StringBuilder();
                record.append(nightlyPrices.getPropertyInfo().getPropertyId()).append(",")
                        .append(nightlyPrices.getPropertyInfo().getPropertyName().replaceAll(",","")).append(",")
                        .append(nightlyPrices.getPropertyInfo().getPropertyType().replaceAll(",","")).append(",")
                        .append(nightlyPrices.getPropertyInfo().getPricePerNight()).append(",");
                if(nightlyPrices.getPrices() == null){
                    for(int i=0; i<365; i++){
                        record.append(nightlyPrices.getPropertyInfo().getPricePerNight()).append(",");
                    }
                }else {
                    for (int i = 0; i < Math.min(365, nightlyPrices.getPrices().size()); i++) {
                        record.append(nightlyPrices.getPrices().get(i)).append(",");
                    }
                }

                outputStream.write(record.append("\n").toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void downloadCSVFileForThreeDates(List<List<String>> priceDateInfo, OutputStream outputStream) throws IOException {
        String headLine = "PropertyName , highestPrice, firstDate, secondDate, thirdDate\n";
        outputStream.write(headLine.getBytes());

        priceDateInfo.stream().forEach(priceDate->{
            try {
                outputStream.write(new StringBuilder().append(priceDate.get(4).replaceAll(",","")).append(",")
                                .append(priceDate.get(0)).append(",")
                                .append(priceDate.get(1)).append(",")
                                .append(priceDate.get(2)).append(",")
                        .append(priceDate.get(3)).append("\n").toString().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
