package com.mysite.weather;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public String home() {
        return "index";
    }
    
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/address")
    public String showWeather(@RequestParam("address") String address, Model model)  {
        Map<String, String> lanLon = weatherService.returnLanLon(address);

        model.addAttribute("weather", weatherService.returnWeather(lanLon));
        return "index";
    }
}