package com.mysite.weather;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    // 위도와 경도를 Map<String, String> 형태로 반환하는 메서드
    public Map<String, String> returnLanLon(String address) {
        Map<String, String> coordinates = new HashMap<>();
        String apiKey = "127EBB68-7A9F-383D-943E-A832A673AF77"; // 발급받은 인증키 작성할 것

        try {
            // 주소 인코딩 (인코딩 x 시 400에러 발생함)
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://api.vworld.kr/req/address?service=address&request=getCoord&key="
                    + apiKey + "&type=ROAD&address=" + encodedAddress;

            // URL 객체 생성
            URL url = new URL(apiUrl);
            // 연결 설정
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 응답 코드 확인
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                // 응답 내용 읽기
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // JSON 응답 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.getJSONObject("response").getString("status").equals("OK")) {
                    String x = jsonResponse.getJSONObject("response").getJSONObject("result").getJSONObject("point").getString("x");
                    String y = jsonResponse.getJSONObject("response").getJSONObject("result").getJSONObject("point").getString("y");
                    coordinates.put("lat", y);
                    coordinates.put("lon", x);
                }
            } else {
                System.out.println("Error: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return coordinates;
    }

	// 날씨 정보를 Map<String, String> 형태로 반환하는 메서드
    public Map<String, String> returnWeather(Map<String, String> lanLon) {
        String apiKey = "c94a0f7ef04cb5f8ddad941816318fb2"; // 발급받은 API 키 작성할 것
        
        // 넘어온 위도, 경도를 포함한 url
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?lat="
                + lanLon.get("lat") + "&lon=" + lanLon.get("lon") + "&appid=" + apiKey;

        Map<String, String> weather = new HashMap<>();

        try {
            // URL 객체 생성
            URL url = new URL(apiUrl);
            // 연결 설정
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 응답 코드 확인
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                // 응답 내용 읽기
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // JSON 응답 파싱
                JSONObject jsonResponse = new JSONObject(response.toString());

                weather.put("weather_main", jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main"));
                weather.put("weather_description", jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description"));
                weather.put("weather_icon", jsonResponse.getJSONArray("weather").getJSONObject(0).getString("icon"));
                
                BigDecimal temp = jsonResponse.getJSONObject("main").getBigDecimal("temp");
                BigDecimal tempCelsius = BigDecimal.valueOf(temp.doubleValue() - 273.15).setScale(2, RoundingMode.HALF_UP); // 소수점 둘째 자리에서 반올림

                weather.put("temperature", String.valueOf(tempCelsius));

                int humidity = jsonResponse.getJSONObject("main").getInt("humidity");
                weather.put("humidity", Integer.toString(humidity) + "%");
                
                String cityName = jsonResponse.getString("name");
                weather.put("name", cityName);
            } else {
                System.out.println("Error: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return weather;
    }

}
