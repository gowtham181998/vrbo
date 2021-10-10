package com.example.demo.service;

import com.example.demo.model.PropertyInfo;
import com.example.demo.request.SampleRequestBuilder;
import com.example.demo.response.SampleResponseBuilder;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class VrboServiceImpl implements VrboService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private RestTemplate restTemplate;

    private SampleRequestBuilder sampleRequestBuilder;

    private SampleResponseBuilder sampleResponseBuilder;

    VrboServiceImpl(RestTemplate restTemplate, SampleRequestBuilder sampleRequestBuilder, SampleResponseBuilder sampleResponseBuilder){
        this.restTemplate = restTemplate;
        this.sampleRequestBuilder = sampleRequestBuilder;
        this.sampleResponseBuilder = sampleResponseBuilder;
    }

    @Override
    @Cacheable(cacheNames = "closest-properties", unless = "#result == null")
    public List<PropertyInfo> getClosestPlaces(String place, int radius){

        JSONObject payLoad = sampleRequestBuilder.buildRequestPayLoad(place);

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);
        restTemplate.setMessageConverters(messageConverters);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        httpHeaders.setContentLength(payLoad.toString().length());
        httpHeaders.setConnection("keep-alive");
        List<MediaType> list = new ArrayList<MediaType>();
        list.add(MediaType.valueOf(MediaType.ALL_VALUE));
        httpHeaders.setAccept(list);

        ResponseEntity<Map> response = restTemplate.exchange("https://www.vrbo.com/serp/g", HttpMethod.POST, new HttpEntity<>(payLoad,httpHeaders), Map.class);

        return sampleResponseBuilder.buildResponseForBulk(response.getBody(),radius);
    }

}
