package com.ai.lxy.weather.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
@CrossOrigin(origins = "*") // 开发阶段先这么写，生产别用*
@RestController
@RequestMapping("/api/weather")
/**
 * @Description TODO
 * @User JeremySong
 * @Date 2026/1/28 11:21
 * @Version 1.0
 */
public class WeatherControllerNew {
    private final RestClient restClient = RestClient.create();
    @GetMapping("/now/yh")
    public WeatherView now(@RequestParam String city) {
        String url = "https://wttr.in/" + city + "?format=j1";

        WttrResponse resp = restClient.get()
                .uri(url)
                .retrieve()
                .body(WttrResponse.class);

        if (resp == null || resp.current_condition == null || resp.current_condition.isEmpty()) {
            WeatherView v = new WeatherView();
            v.city = city;
            v.error = "No data";
            return v;
        }

        var c = resp.current_condition.get(0);
        WeatherView v = new WeatherView();
        v.city = city;
        v.tempC = c.temp_C;
        v.feelsLikeC = c.FeelsLikeC;
        v.humidity = c.humidity;
        v.windSpeedKmph = c.windspeedKmph;
        v.desc = (c.weatherDesc != null && !c.weatherDesc.isEmpty()) ? c.weatherDesc.get(0).value : "";
        return v;
    }

    // -------- DTOs --------

    public static class WeatherView {
        public String city;
        public String tempC;
        public String feelsLikeC;
        public String desc;
        public String humidity;
        public String windSpeedKmph;
        public String error;
    }

    public static class WttrResponse {
        public List<CurrentCondition> current_condition;
    }

    public static class CurrentCondition {
        public String temp_C;
        public String FeelsLikeC;
        public List<WeatherDesc> weatherDesc;
        public String humidity;
        public String windspeedKmph;
    }

    public static class WeatherDesc {
        public String value;
    }
}
