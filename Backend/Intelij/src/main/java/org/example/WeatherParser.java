package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.WeatherData;

public class WeatherParser {

    public static WeatherData parseWeatherData(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Extract required fields with null checks
        JsonObject data = jsonObject.getAsJsonObject("data");
        String time = data != null && data.has("time") ? data.get("time").getAsString() : "Unknown time";

        JsonObject values = data != null ? data.getAsJsonObject("values") : null;
        String temp = values != null && values.has("temperature") ? values.get("temperature").getAsString() : "Unknown temperature";
        String weatherCode = values != null && values.has("weatherCode") ? values.get("weatherCode").getAsString() : "Unknown weather code";

        JsonObject location = jsonObject.getAsJsonObject("location");
        String lat = location != null && location.has("lat") ? location.get("lat").getAsString() : "Unknown latitude";
        String lon = location != null && location.has("lon") ? location.get("lon").getAsString() : "Unknown longitude";
        String locationCor = lat + ", " + lon;

        // For locationName, assume a placeholder since it's not in the JSON
        String locationName = "Unknown location";

        // Populate and return WeatherData
        return new WeatherData(time, temp, weatherCode, locationCor, locationName);
    }

    public static void main(String[] args) {
        // For testing purposes, you can call the parseWeatherData here
    }
}
