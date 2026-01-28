package com.ai.lxy.weather.controller;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final RestClient rest = RestClient.create();

    @GetMapping("/current")
    public WeatherResponse current(@RequestParam String city,
                                   @RequestParam(required = false) String countryCode) {
        if (!StringUtils.hasText(city)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "city is required");
        }

        // 1) Geocoding: city -> lat/lon
        String geoUrl = UriComponentsBuilder
                .fromHttpUrl("https://geocoding-api.open-meteo.com/v1/search")
                .queryParam("name", city)
                .queryParam("count", 1)
                .queryParam("language", "en")
                .queryParam("format", "json")
                .queryParamIfPresent("country_code",
                        StringUtils.hasText(countryCode) ? java.util.Optional.of(countryCode) : java.util.Optional.empty())
                .toUriString();

        GeoResponse geo = rest.get()
                .uri(geoUrl)
                .retrieve()
                .body(GeoResponse.class);

        if (geo == null || geo.results == null || geo.results.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found: " + city);
        }

        GeoResult loc = geo.results.get(0);

        // 2) Forecast: lat/lon -> current_weather
        String weatherUrl = UriComponentsBuilder
                .fromHttpUrl("https://api.open-meteo.com/v1/forecast")
                .queryParam("latitude", loc.latitude)
                .queryParam("longitude", loc.longitude)
                .queryParam("current_weather", true)
                // timezone=auto 让时间更贴近当地（可选）
                .queryParam("timezone", "auto")
                .toUriString();

        ForecastResponse forecast = rest.get()
                .uri(weatherUrl)
                .retrieve()
                .body(ForecastResponse.class);

        if (forecast == null || forecast.current_weather == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Weather API returned empty data");
        }

        var cw = forecast.current_weather;

        WeatherResponse resp = new WeatherResponse();
        resp.city = loc.name;
        resp.country = loc.country;
        resp.latitude = loc.latitude;
        resp.longitude = loc.longitude;

        resp.time = cw.time;
        resp.temperatureC = cw.temperature;
        resp.windspeedKmh = cw.windspeed;
        resp.winddirectionDeg = cw.winddirection;
        resp.weathercode = cw.weathercode;

        return resp;
    }

    // ---------------- DTOs ----------------

    public static class WeatherResponse {
        public String city;
        public String country;
        public double latitude;
        public double longitude;

        public String time;            // e.g. 2026-01-28T10:00
        public double temperatureC;    // 摄氏度
        public double windspeedKmh;    // km/h
        public double winddirectionDeg;
        public int weathercode;        // Open-Meteo weather code
    }

    // Geocoding response
    public static class GeoResponse {
        public List<GeoResult> results;
    }

    public static class GeoResult {
        public String name;
        public String country;
        public double latitude;
        public double longitude;
    }

    // Forecast response
    public static class ForecastResponse {
        public CurrentWeather current_weather;
    }

    public static class CurrentWeather {
        public String time;
        public double temperature;
        public double windspeed;
        public double winddirection;
        public int weathercode;
    }


    @GetMapping("/now")
    public String weather(@RequestParam String city) {
        String url = "https://wttr.in/" + city + "?format=j1";
        return rest.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }
}
