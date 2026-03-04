package com.ai.lxy.weather.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/weather")
public class WeatherControllerApiKey {

    private static final String KEY = "d84b09fc32834de5812f9321f7c9bed7";

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/key")
    public Object weather(@RequestParam String city) {

        // 1 查 location
        String geoUrl = "https://geoapi.qweather.com/v2/city/lookup?location="
                + city + "&key=" + KEY;

        Map geo = restTemplate.getForObject(geoUrl, Map.class);

        List locations = (List)((Map)geo).get("location");

        if(locations == null || locations.isEmpty()){
            return Map.of("error","城市不存在");
        }

        Map first = (Map) locations.get(0);
        String locationId = (String) first.get("id");

        // 2 查天气
        String weatherUrl =
                "https://devapi.qweather.com/v7/weather/now?location="
                        + locationId + "&key=" + KEY;

        return restTemplate.getForObject(weatherUrl, Map.class);
    }
}