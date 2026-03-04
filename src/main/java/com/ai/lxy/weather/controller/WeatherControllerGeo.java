package com.ai.lxy.weather.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @User JeremySong
 * @Date 2026/3/4 16:33
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/weather")
@CrossOrigin
public class WeatherControllerGeo {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/lxy")
    public Map<String,Object> getWeather(@RequestParam String city){

        Map<String,Object> result = new HashMap<>();

        // 1 查询经纬度
        String geoUrl =
                "https://geocoding-api.open-meteo.com/v1/search?name="
                        + city + "&count=1&language=zh&format=json";

        JsonNode geo = restTemplate.getForObject(geoUrl, JsonNode.class);

        if(geo == null || geo.get("results")==null){
            result.put("error","城市不存在");
            return result;
        }

        JsonNode location = geo.get("results").get(0);

        double lat = location.get("latitude").asDouble();
        double lon = location.get("longitude").asDouble();

        // 2 查询天气
        String weatherUrl =
                "https://api.open-meteo.com/v1/forecast?latitude="
                        + lat + "&longitude=" + lon
                        + "&current_weather=true";

        JsonNode weather =
                restTemplate.getForObject(weatherUrl, JsonNode.class);

        result.put("city",city);
        result.put("latitude",lat);
        result.put("longitude",lon);
        result.put("weather",weather.get("current_weather"));

        return result;
    }
}
