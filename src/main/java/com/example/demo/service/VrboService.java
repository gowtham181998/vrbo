package com.example.demo.service;

import com.example.demo.model.PropertyInfo;

import java.util.List;

public interface VrboService {

    List<PropertyInfo> getClosestPlaces(String place, int radius);

}
