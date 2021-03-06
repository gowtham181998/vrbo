package com.example.demo.service;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;

public interface WebScraperService {
    List<Long> getPrices(String url) throws IOException, ParseException;
}
