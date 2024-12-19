package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
public class WeatherParser {

    public static WeatherData parseWeatherData(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Packa upp JSON objekt och skapa WeatherData objekt med
        JsonObject data = jsonObject.getAsJsonObject("data");
        String time = data != null && data.has("time") ? data.get("time").getAsString() : "Unknown time";

        JsonObject values = data != null ? data.getAsJsonObject("values") : null;
        String temp = values != null && values.has("temperature") ? values.get("temperature").getAsString() : "Unknown temperature";
        String weatherCode = values != null && values.has("weatherCode") ? values.get("weatherCode").getAsString() : "Unknown weather code";

        JsonObject location = jsonObject.getAsJsonObject("location");
        String lat = location != null && location.has("lat") ? location.get("lat").getAsString() : "Unknown latitude";
        String lon = location != null && location.has("lon") ? location.get("lon").getAsString() : "Unknown longitude";
        String locationCor = lat + ", " + lon;

        String locationName = "Unknown location";

        //return WeatherData
        return new WeatherData(time, temp, weatherCode, locationCor, locationName);
    }

}